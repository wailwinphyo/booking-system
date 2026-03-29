#!/bin/bash

# Script to create and verify users

# Assume app is running on http://localhost:8080

for i in {1..50}
do
    EMAIL="testuser$i@example.com"
    echo "Creating user $i: $EMAIL"

    # 1. Register a new user
    echo "Registering user..."
    curl -X POST http://localhost:8080/auth/register \
      -d "email=$EMAIL&password=password"

    echo -e "\n"

    # 2. Verify user
    echo "Verifying user..."
    curl -X POST http://localhost:8080/auth/verify \
      -d "email=$EMAIL&token=verify"

    echo -e "\n"

    echo "User $i created and verified."
    echo -e "\n\n"
done

echo "All 50 users created."