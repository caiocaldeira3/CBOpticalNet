import dataclasses as dc
from pathlib import Path

import numpy as np
import pandas as pd


@dc.dataclass(init=True)
class DataReader:
    dataset: str = dc.field(init=True)
    project: str = dc.field(init=True)
    num_nodes: int = dc.field(init=True)
    switch_size: int = dc.field(init=True)
    mirrored: str = dc.field(init=True)
    num_sims: int = dc.field(init=True)
    mu: int = dc.field(init=True, default=None)
    switch_ports: int = dc.field(init=False)
    num_switches_1: int = dc.field(init=False)
    num_switches_2: int = dc.field(init=False)
    max_rounds: int = dc.field(init=False)

    def __post_init__ (self) -> None:
        self.max_rounds = -1
        for sim_id in range(1, self.num_sims + 1):
            with open(self.file_path / f"{sim_id}/simulation_info.txt") as sim_file:
                self.max_rounds = max(
                    self.max_rounds, int(sim_file.readline().split(",")[1])
                )

        with open(self.file_path / f"{sim_id}/simulation_info.txt") as sim_file:
            sim_file.readline()

            _, clusters_1, _, c1_size = sim_file.readline().split(",")
            _, clusters_2, _, c2_size = sim_file.readline().split(",")
            clusters_1, c1_size, clusters_2, c2_size = map(
                int, (clusters_1, c1_size, clusters_2, c2_size)
            )

            self.num_switches_1 = clusters_1 * c1_size
            self.num_switches_2 = clusters_2 * c2_size

        self.switch_ports = self.switch_size // 2

    @property
    def num_switches (self) -> int:
        return self.num_switches_1 + self.num_switches_2

    @property
    def node_ports (self) -> int:
        return 4 * self.num_switches_1

    @property
    def file_path (self) -> Path:
        return Path(
            Path(__file__).parent.parent /
            f"logs/output/{self.dataset}/{self.project}_{self.num_nodes}/{self.switch_size}/{self.mirrored}/{self.mu}/"
        )

    def cdf_active_switches (self) -> np.ndarray:
        cdf = np.zeros(self.num_switches, dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/routings.csv")
        alt_df = pd.read_csv(self.file_path / "1/alterations.csv")

        active_df = pd.concat(
            [rout_df[["round", "switch"]], alt_df[["round", "switch"]]]
        ).drop_duplicates().reset_index(drop=True)

        for _, row in active_df.iterrows():
            cdf[row["switch"]] += 1

        return cdf / 10**3

    def cdf_active_ports (self) -> np.ndarray:
        cdf = np.zeros((self.num_switches, self.switch_ports), dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/routings.csv")
        alt_df = pd.read_csv(self.file_path / "1/alterations.csv").rename(
            columns={"node": "from_node"}
        )

        active_df = pd.concat(
            [rout_df[["switch", "from_node"]], alt_df[["switch", "from_node"]]]
        ).reset_index(drop=True)

        for _, row in active_df.iterrows():
            cdf[ row["switch"], row["from_node"] % self.switch_ports ] += 1

        return cdf.reshape(-1)

    def cdf_switch_active_ports (self) -> np.ndarray:
        cdf = np.zeros((self.num_switches, self.switch_ports), dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/routings.csv")
        alt_df = pd.read_csv(self.file_path / "1/alterations.csv").rename(
            columns={"node": "from_node"}
        )

        active_df = pd.concat(
            [rout_df[["switch", "from_node"]], alt_df[["switch", "from_node"]]]
        ).drop_duplicates().reset_index(drop=True)

        for _, row in active_df.iterrows():
            cdf[ row["switch"], row["from_node"] % self.switch_ports ] += 1

        return np.sum(cdf, axis=1).T / self.switch_ports

    def cdf_node_routings (self) -> np.ndarray:
        cdf = np.zeros(self.num_nodes, dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/routings.csv")
        for _, row in rout_df.iterrows():
            cdf[row["from_node"]] += 1
            cdf[row["to_node"]] += 1

        return cdf / 10**3

    def cdf_switch_routings (self) -> np.ndarray:
        cdf = np.zeros(self.num_switches, dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/routings.csv")
        for _, row in rout_df.iterrows():
            cdf[row["switch"]] += 1

        return cdf

    def cdf_node_alterations (self) -> np.ndarray:
        cdf = np.zeros(self.num_nodes, dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/alterations.csv")
        for _, row in rout_df.iterrows():
            cdf[row["node"]] += 1

        return cdf

    def cdf_switch_alterations (self) -> np.ndarray:
        cdf = np.zeros(self.num_switches, dtype=np.float64)

        rout_df = pd.read_csv(self.file_path / "1/alterations.csv")
        for _, row in rout_df.iterrows():
            cdf[row["switch"]] += 1

        return cdf

    def read_operations (self) -> tuple[
        np.ndarray, np.ndarray, np.ndarray, np.ndarray
    ]:
        total_routing = np.empty(self.num_sims)
        total_link_alterations = np.empty(self.num_sims)
        total_swt_alterations = np.empty(self.num_sims)
        total_rotations = np.empty(self.num_sims)

        for sim_id in range(1, self.num_sims + 1):
            file_df = pd.read_csv(self.file_path / f"{sim_id}/operations.csv")

            total_routing[sim_id - 1] = file_df.loc[
                file_df.name == "message-routing", "sum"
            ].item()

            total_swt_alterations[sim_id - 1] = file_df.loc[
                file_df.name == "swt-alteration", "sum"
            ].item()

            total_link_alterations[sim_id - 1] = file_df.loc[
                file_df.name == "link-alteration", "sum"
            ].item()

            total_rotations[sim_id - 1] = file_df.loc[
                file_df.name == "rotation", "sum"
            ].item()

        print(f"finish reading {self.num_nodes}|{self.switch_size}")
        return (
            total_routing, total_link_alterations,
            total_swt_alterations, total_rotations
        )

    def read_throughput (self) -> np.ndarray:
        raw = np.zeros((self.num_sims, self.max_rounds), dtype=np.ndarray)

        for sim_id in range(1, self.num_sims + 1):
            file_df = pd.read_csv(self.file_path / f"{sim_id}/throughput.csv")

            for _, row in file_df.iterrows():
                raw[row["round"]] += row["completed_requests"]
                raw[row["round"]] += row["completed_requests"]

        raw = np.array([ np.pad(line, (0, self.max_rounds - len(line))) for line in raw ])
        completed_requests = np.sum(raw, axis=0) / self.num_sims

        print(f"finish reading {self.num_nodes}|{self.switch_size}")
        return completed_requests

    def read_active_ports (self, sims: int = None) -> None:
        sims = list(range(1, self.num_sims + 1)) if sims is None else [ sims ]
        active_ports = np.zeros((self.num_switches, self.max_rounds))

        for sim_id in sims:
            file_df = pd.read_csv(self.file_path / f"{sim_id}/switches_active_ports_per_round.csv")

            for _, row in file_df.iterrows():
                active_ports[row["switch"], row["round"]] += row["active_ports"]
                active_ports[row["switch"] + 1, row["round"]] += row["active_ports"]

        for switch in active_ports:
            for i in range(1, self.max_rounds):
                switch[i] += switch[i - 1]

        print(f"finish reading {self.num_nodes}|{self.switch_size}")
        return active_ports / len(sims)
