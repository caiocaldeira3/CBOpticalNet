#! /usr/bin/env python
import os
import threading

import numpy
from util import check_sim

# this file keep all completed experiments
log_path = "./scripts/logs/"
log_file = "skewedLog.txt"

if not os.path.exists(log_path):
    os.makedirs(log_path)

# read log file
open(os.path.join(log_path, log_file), 'a').close()
log = {line.rstrip() for line in open(os.path.join(log_path, log_file))}

# open log file for append and create a lock variable
file = open("scripts/logs/skewedLog.txt", "a+")
file_lock = threading.Lock()

projects = [ "cbOptNet" ]

# parameters of simulation
num_nodes = [ 128 ]
switch_sizes = [ 16 ]
sequential = [ "false" ]
mirrored = [ "true", "false"]
mus = [ 4 ]
num_simulations = 30

x = [1] #skewed
y = [0.4] #skewed

#number of threads to simulation
num_threads = 2

java = "java"
classpath = "binaries/bin:binaries/jdom.jar"
program = "sinalgo.Run"
args = " ".join(["-batch", "-project"])
base_cmd = f"{java} -cp {classpath} {program} {args}"

#extends thread class
class Threader (threading.Thread):
    threadID: int
    commands: list[str]

    def __init__ (self, threadID: int, commands: list[str]) -> None:
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.commands = commands

    def run(self) -> None:
        for command in self.commands:
            self.execute(command)

    def execute (self, command) -> None:
        print(command)
        os.system(command)

        sim_file = command.split(" > ")[-1]
        if not check_sim(sim_file):
            file_lock.acquire()
            file.write(f"Error with {command}\n")
            file_lock.release()

#for each project executed
for project in projects:
    commands = []

    # generate all possibles inputs for simulation
    for num_node in num_nodes:
        for idx in x:
            for idy in y:
                for sim_id in range(1, num_simulations + 1):
                    for switch_size in switch_sizes:
                        for mu in mus:
                            for mirror in mirrored:
                                for sequentiality in sequential:
                                    if switch_size == -1:
                                        switch_size = 2 * num_node

                                    elif switch_size == 256 and num_nodes == 128:
                                        continue

                                    elif switch_size <= 16 and num_node >= 256:
                                        continue

                                    elif switch_size <= 64 and num_node >= 512:
                                        continue

                                    dataset = f"{idx}-{idy}"
                                    input_file = (
                                        f"input/bursty/{dataset}/{num_node}/{sim_id}_tor_{num_node}.txt"
                                    )

                                    mirror_path = "mirrored" if mirror == "true" else "generic"
                                    output_path = (
                                        "output/skewed-" +
                                        f"{dataset}/{project}_{num_node}/{switch_size}/{mirror_path}/{mu}/{sim_id}/"
                                    )
                                    sim_stream = f"logs/{output_path}sim.txt"

                                    if not os.path.exists(f"logs/{output_path}"):
                                        os.makedirs(f"logs/{output_path}")

                                    cmd = (
                                        f"time --verbose {base_cmd} {project} -overwrite mu={mu} input=" \
                                        f"{input_file} switchSize={switch_size}  output={output_path} " \
                                        f"isSequential={sequentiality} mirrored={mirror} AutoStart=true > {sim_stream}"
                                    )

                                    print(cmd)
                                    commands.append(cmd)

    num_commands = len(commands)

    # if number of threads is greater than pairsLenght
    # just makes number of threads equals to pairsLenght
    if num_commands == 0:
        print(f"No experiment to be executed for project {project}")
        exit
    elif num_threads > num_commands:
        num_threads = num_commands

    # split task for threads
    size = num_commands // num_threads
    chunks =  numpy.array_split(commands, num_threads)

    threads = []
    threadID = 1

    # Create new threads
    for idx in range(0, num_threads):
        thread = Threader(threadID, chunks[idx])
        thread.start()
        threads.append(thread)
        threadID += 1


    # Wait for all threads to complete
    for t in threads:
        t.join()


print("Simulation Completed")
