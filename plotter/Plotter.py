import dataclasses as dc

import matplotlib.pyplot as plt
import numpy as np
import statsmodels.api as sm
from DataReader import DataReader

plt.style.use(["science","ieee"])

@dc.dataclass()
class Plotter:
    def total_work (
        plot_data: list[DataReader], normalize: int = 1, ax: plt.axes = None
    ) -> None:
        project_names = []
        routing_means = []
        alteration_means = []
        work_stds = []

        for data in plot_data:
            project = "CBN" if data.project == "cbOptNet" else "DSN"
            mirror_name = (data.mirrored if data.mirrored == "mirrored" else "augpath")
            dataset = data.dataset.split("-")[0]
            project_name = f"{project}--{dataset}--{mirror_name}"

            total_routing, total_alteration, total_work = data.read_operations()

            project_names.append(project_name)
            routing_means.append(total_routing.mean() / normalize)
            alteration_means.append(total_alteration.mean() / normalize)
            work_stds.append((total_work / normalize).std())

        if ax is None:
            fig, ax = plt.subplots(figsize=(8, 4))
            ax.legend(loc="right")
            ax.set_title("Total Work")
            ax.set_xlabel("Project")
            ax.set_ylabel("Work * 10 ^ 4")

        ax.bar(project_names, routing_means, label="routing", color=["silver"])
        ax.bar(project_names, alteration_means, yerr=work_stds, bottom=routing_means, label="alterations",  color=["grey"])
        ax.legend(loc="best")

    def cdf_active_switches (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
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

    def cdf_active_ports (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
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

    def cdf_switches_active_ports (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
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

    def cdf_routings (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
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

    def cdf_alterations (cdf_array: np.ndarray, ax: plt.axes = None) -> None:
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