#!/bin/bash

echo "copy a file from turtle home to local home"
sshpass -p 'powin' scp  -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -r powin@10.0.0.3:/home/powin/$1 /home/powin/$2
