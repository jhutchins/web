#!/bin/bash

parallel_test() {
    for ((i=1;i<=100;i++));
    do
        curl http://localhost:8000/test > /dev/null 2>&1 &  
    done

    for job in `jobs -p`
    do
        wait $job
    done
}

sequential_test() {
    for ((i=1;i<=100;i++));
    do
        curl http://localhost:8000/test > /dev/null 2>&1
    done
}

# Setup
dd if=/dev/zero of=./test bs=1024 count=10240 > /dev/null 2>&1

# Test
echo "Parallel Test 100 requests for 10MB file"
time parallel_test
echo
echo
echo "Sequential Test 100 requests for 10MB file"
time sequential_test

# Clean up
rm ./test
