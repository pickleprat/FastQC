#!/usr/bin/env bash
#
# Convenience launcher for a SOURCE build of FastQC (classes compiled into bin/).
# The bundled `fastqc` wrapper refuses to run when source .java files are present
# and expects compiled classes in the repo root, so it can't be used here.
#
# Usage:
#   ./run-fastqc.sh                          # launch the interactive GUI
#   ./run-fastqc.sh file1.fastq [file2 ...]  # non-interactive report(s) -> ./fastqc_reports
#
# Any extra FastQC -D properties can be set via the JAVA_OPTS env var, e.g.:
#   JAVA_OPTS="-Dfastqc.unzip=true -Dfastqc.threads=4" ./run-fastqc.sh sample.fastq.gz

set -euo pipefail

base_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$base_dir"

if [[ ! -d bin ]]; then
    echo "[ERROR] No compiled classes found in bin/. Run 'ant' first to build." >&2
    exit 1
fi

CP="bin:htsjdk.jar:commons-io-2.22.0.jar:commons-compress-1.28.0.jar:cisd-jhdf5.jar"
MAIN="uk.ac.babraham.FastQC.FastQCApplication"

if [[ $# -eq 0 ]]; then
    exec java -Dsun.java2d.xrender=false ${JAVA_OPTS:-} -cp "$CP" "$MAIN"
else
    # Non-interactive mode: write reports to ./fastqc_reports
    outdir="$base_dir/fastqc_reports"
    mkdir -p "$outdir"
    exec java -Djava.awt.headless=true -XX:ParallelGCThreads=1 \
        -Dfastqc.output_dir="$outdir" ${JAVA_OPTS:-} \
        -cp "$CP" "$MAIN" "$@"
fi
