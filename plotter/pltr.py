import dataclasses as dc

import matplotlib.pyplot as plt
import numpy as np
import statsmodels.api as sm
from datareader import DataReader

plt.style.use(["science", "ieee"])

abbr: dict[str, str] = {
    "cbOptNet": "CBN",
    "displayOpticNet": "ODSN",
    "semiDisplayOpticNet": "DSN"
}

@dc.dataclass()
class Plotter:
    @classmethod
    def get_project_name (cls, data: DataReader) -> str:
        project = abbr[data.project]
        if data.mirrored != "mirrored":
            return rf"OpticNet$^{{AP}}$({project})"
        else:
            return f"OpticNet({project})"

    @classmethod
    def total_work_link_updates (
        cls, plot_data: list[DataReader], normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        routing_means = []
        alteration_means = []
        work_stds = []

        for data in plot_data:
            project_name = cls.get_project_name(data)

            total_routing, total_link_updates, _, _ = data.read_operations()
            total_work = total_routing + total_link_updates

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            alteration_means.append(total_link_updates.mean() / normalize)
            work_stds.append((total_work / normalize).std())

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work Link Updates")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(project_names, routing_means, label="Service Cost", color=["silver"])
        ax.bar(
            project_names, alteration_means, yerr=work_stds,
            bottom=routing_means, label="Link Updates",  color=["grey"]
        )
        ax.legend(loc="best")

    @classmethod
    def total_work_swt_updates (
        cls, plot_data: list[DataReader], normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        routing_means = []
        alteration_means = []
        work_stds = []

        for data in plot_data:
            project_name = cls.get_project_name(data)

            total_routing, _, total_swt_updates, _ = data.read_operations()
            total_work = total_routing + total_swt_updates

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            alteration_means.append(total_swt_updates.mean() / normalize)
            work_stds.append((total_work / normalize).std())

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work Swt Updates")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(project_names, routing_means, label="Service Cost", color=["silver"])
        ax.bar(
            project_names, alteration_means, yerr=work_stds,
            bottom=routing_means, label="Switch Updates",  color=["grey"]
        )
        ax.legend(loc="best")

    @classmethod
    def total_work_rotation (
        cls, plot_data: list[DataReader], normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        routing_means = []
        rotation_means = []
        work_stds = []

        for data in plot_data:
            project_name = cls.get_project_name(data)

            total_routing, _, _, total_rotation = data.read_operations()
            total_work = total_rotation + total_routing

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            rotation_means.append(total_rotation.mean() / normalize)
            work_stds.append((total_work / normalize).std())

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work Rotations")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(project_names, routing_means, label="routing", color=["silver"])
        ax.bar(
            project_names, rotation_means, yerr=work_stds,
            bottom=routing_means, label="rotations",  color=["grey"]
        )
        ax.legend(loc="best")

    @classmethod
    def cdf_active_switches (cls, cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Switches ativos por mais que (x) rounds")
            ax.set_xlabel("Rounds x 10**4")
            ax.set_ylabel("Porcentagem dos switches ativos")

        min_indx = max(min(cdf_array - 0.1), 0)
        x = np.linspace(min_indx, max(cdf_array))
        y = ecdf(x)

        return ax.step(x, y)

    @classmethod
    def cdf_active_ports (cls, cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF Portas ativas por mais que (x) rounds")
            ax.set_xlabel("Rounds")
            ax.set_ylabel("Porcentagem de rounds por portas ativas")

        x = np.linspace(0, max(cdf_array) + 100)
        y = ecdf(x)

        return ax.step(x, y)

    @classmethod
    def cdf_switches_active_ports (cls, cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF % portas ativas por Switch")
            ax.set_xlabel("Porcentagem de portas ativas")
            ax.set_ylabel("Porcentagem dos switches")

        min_indx = max(min(cdf_array - 0.1), 0)
        max_indx = max(cdf_array + 0.1)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)

    @classmethod
    def cdf_routings (cls, cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF roteamentos por objeto")
            ax.set_xlabel("Roteamentos x 10^3")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(cdf_array - 10), 0)
        max_indx = max(cdf_array + 10)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)

    @classmethod
    def cdf_alterations (cls, cdf_array: np.ndarray, ax: plt.axes = None) -> None:
        ecdf = sm.distributions.empirical_distribution.ECDF(cdf_array)

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("CDF alterações por objeto")
            ax.set_xlabel("Alterações")
            ax.set_ylabel("Porcentagem dos objetos")

        min_indx = max(min(cdf_array - 2), 0)
        max_indx = max(cdf_array + 2)
        x = np.linspace(min_indx, max_indx)
        y = ecdf(x)

        return ax.step(x, y)
