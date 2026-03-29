#!/bin/bash

# Script to purchase packages for existing users

# Assume app is running on http://localhost:8080

for i in {1..50}
do
    EMAIL="testuser$i@example.com"
    echo "Purchasing for user $i: $EMAIL"

    # 1. Login
    echo "Logging in..."
    TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -u $EMAIL:password)
    echo "Token: $TOKEN"

    echo -e "\n"

    # 2. Purchase a package (vary by user)
    MOD=$((i % 4))
    if [ $MOD -eq 1 ]; then
        PACKAGE_ID=1  # SG Basic
        COUNTRY="Singapore"
    elif [ $MOD -eq 2 ]; then
        PACKAGE_ID=2  # SG Premium
        COUNTRY="Singapore"
    elif [ $MOD -eq 3 ]; then
        PACKAGE_ID=3  # MM Basic
        COUNTRY="Myanmar"
    else
        PACKAGE_ID=4  # MM Premium
        COUNTRY="Myanmar"
    fi
    echo "Purchasing package $PACKAGE_ID for $COUNTRY..."
    curl -X POST "http://localhost:8080/packages/purchase?email=$EMAIL&packageId=$PACKAGE_ID&cardDetails=1234567890123456" \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 3. Get user packages
    echo "Getting user packages..."
    curl -X GET http://localhost:8080/packages/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    echo "User $i purchased package."
    echo -e "\n\n"
done

echo "All 50 users purchased packages."