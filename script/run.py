#!/usr/bin/env python2.7

import time
from subprocess import Popen, PIPE

Ultimate_path = "/home/ultimate/ultimate"
Ultimate_bin = Ultimate_path + "/script/testSingle.sh"
log = open(Ultimate_path + "/script/result.log", "w")
Max_time = 200

def do_log(line):
    print line
    log.write(line + "\n")


def wait_timeout(proc, seconds, filename):
    """Wait for a process to finish, or raise exception after timeout"""
    start = time.time()
    end = start + seconds
    interval = .25
    process_time = 0

    while True:
        result = proc.poll()
        if result is not None:
            do_log("+ time: " + str(process_time))
            do_log("+ " + proc.communicate()[0])
            return result
        if time.time() >= end:
            proc.kill()
            do_log("Process timed out\n")
            return result
        process_time += interval
        time.sleep(interval)

def analysis_file(filename):
    do_log(filename)
    cmd = [Ultimate_bin, filename]
    run = Popen(cmd, stdout=PIPE)
    result = wait_timeout(run, Max_time, filename)

def main():
    file_list = open(Ultimate_path + "/script/term_list64.txt");

    for line in file_list.readlines():
        analysis_file(Ultimate_path + line.split("\n")[0])


if __name__ == "__main__":
    main()
