#!/bin/bash

# Start the main process and save its PID
# Use exec to replace the shell script process with the main process
exec java -jar -Dspring.profiles.active="$ENV" /app/gameserver.jar &
pid=$!

# Trap the SIGTERM signal and forward it to the main process
trap 'kill -15 $pid; wait $pid' SIGTERM

# Wait for the main process to complete
wait $pid

