import os

log_path = "scripts/logs/"
log_file = "failedSimulations.txt"

if not os.path.exists(log_path):
    os.makedirs(log_path)

# open log file for append
failed_log = open(f"{log_path}{log_file}", "a")

def check_sim (sim_path: str) -> bool:
    with open(sim_path) as f:
        if "rotation" in f.read():
            return True

    return False

def ensure_simulations () -> None:
    for subdir, _, files in os.walk("logs/output/"):
        if "sim.txt" in files:
            if check_sim(f"{subdir}/sim.txt"):
                os.remove(f"{subdir}/sim.txt")

            else:
                failed_log.write(f"Failed simulation at {subdir}")

if __name__ == "__main__":
    ensure_simulations()
