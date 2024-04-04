import os
import sys

import scienceplots

scienceplots

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
from datareader import DataReader
from pltr import Plotter

plt.style.use(["science", "ieee"])
mpl.rcParams.update(mpl.rcParamsDefault)
plt.rcParams.update({
    "font.size": 11
})

projects = [ "cbOptNet", "semiDisplayOpticNet", "displayOpticNet" ]
# projects = [ "semiDisplayOpticNet" ]
switch_sizes = [ 16 ]
num_simulations = 1
# datasets = [ "tor", "bursty-0.4-1", "skewed-1-0.4" ]
datasets = [ "facebookDS" ]
mirrored = [ "mirrored" ]
num_nodes = 367
mus = [ 4 ]

if not os.path.exists(f"output/{sys.argv[1]}"):
    os.makedirs(f"output/{sys.argv[1]}")

tor_data = []

for project in projects:
    for dataset in datasets:
        for switch_size in switch_sizes:
            for mirror in mirrored:
                for mu in mus:
                    tor_data.append(
                        DataReader(
                            dataset, project, num_nodes, switch_size,
                            mirror, num_simulations, mu
                        )
                    )

tor_data: np.ndarray[DataReader] = np.array(tor_data)

slc = [ i for i in range(len(tor_data)) ]

print(tor_data[slc])

# fig, ax = plt.subplots(figsize=(7, 4))
# ax.set_ylabel(r"Work $\times 10^4$")

# Plotter.total_work_link_updates(tor_data[slc], normalize=1e4, ax=ax)

# ax.plot()
# fig.savefig(
#     f"output/{sys.argv[1]}/total_work_link_alteration.png",
#     dpi=300, transparent=False
# )
# plt.close(fig)

# print("finish")

# fig, ax = plt.subplots(figsize=(7, 4))
# ax.set_ylabel(r"Work $\times 10^4$")

# Plotter.total_work_swt_updates(tor_data[slc], normalize=1e4, ax=ax)

# ax.plot()
# fig.savefig(
#     f"output/{sys.argv[1]}/total_work_swt_alteration.png",
#     dpi=300, transparent=False
# )
# plt.close(fig)

# print("finish")

fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title(r"% active rounds per switch (n=367, p=8)")
ax.set_xlabel(r"% rounds")
ax.set_ylabel(r"% switches")

for data in tor_data[slc]:
    Plotter.cdf_active_switches(data.cdf_active_switches(), ax)

ax.legend([
    Plotter.get_project_name(data)
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/active_switches.png", dpi=300, transparent=False)
plt.close(fig)

print("finish")

fig, ax = plt.subplots(figsize=(8, 4))
ax.set_title(r"% active ports per switch (n=367, p=8)")
ax.set_xlabel(r"% ports")
ax.set_ylabel(r"% switches")

for data in tor_data[slc]:
    Plotter.cdf_switches_active_ports(data.cdf_switch_active_ports(), ax)

ax.legend([
    Plotter.get_project_name(data)
    for data in tor_data[slc]
], loc="best", frameon=False)

ax.plot()
fig.savefig(f"output/{sys.argv[1]}/switches_active_ports.png", dpi=300, transparent=False)
plt.close(fig)
print("finish")
