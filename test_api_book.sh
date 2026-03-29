#!/bin/bash

# Script to book classes for existing users

# Assume app is running on http://localhost:8080

for i in {1..50}
do
    EMAIL="testuser$i@example.com"
    echo "Booking for user $i: $EMAIL"

    # Determine country based on i
    MOD=$((i % 4))
    if [ $MOD -eq 1 ] || [ $MOD -eq 2 ]; then
        COUNTRY="Singapore"
    else
        COUNTRY="Myanmar"
    fi

    # 1. Login
    echo "Logging in..."
    TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -u $EMAIL:password)
    echo "Token: $TOKEN"

    echo -e "\n"

    # 2. Book multiple times to deplete credits
    for j in {1..5}
    do
        # Get class schedules for the country and pick a random class to book
        echo "Getting $COUNTRY class schedules for booking $j..."
        SCHEDULES_JSON=$(curl -s -X GET http://localhost:8080/bookings/classes/$COUNTRY \
          -H "Authorization: Bearer $TOKEN")

        # Extract class IDs using jq
        CLASS_IDS=$(echo $SCHEDULES_JSON | jq -r '.[].id')
        # Pick a random ID from the list
        RANDOM_CLASS_ID=$(echo "$CLASS_IDS" | shuf -n 1)
        echo "Randomly selected class ID: $RANDOM_CLASS_ID"

        # Create booking
        echo "Attempting to book class $RANDOM_CLASS_ID (attempt $j)..."
        BOOK_RESULT=$(curl -s -X POST http://localhost:8080/bookings/book/$RANDOM_CLASS_ID \
          -H "Authorization: Bearer $TOKEN")
        echo "Booking result: $BOOK_RESULT"

        # If added to waitlist or failed, break
        if [[ $BOOK_RESULT == *"ADDED_TO_WAITLIST"* ]] || [[ $BOOK_RESULT == *"No valid package"* ]]; then
            echo "Stopping bookings for user $i due to $BOOK_RESULT"
            break
        fi

        echo -e "\n"
    done

    # 3. Get user packages after bookings
    echo "Getting user packages after all bookings..."
    curl -X GET http://localhost:8080/packages/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 4. Get user bookings
    echo "Getting user bookings..."
    curl -X GET http://localhost:8080/bookings/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    echo "User $i completed bookings."
    echo -e "\n\n"
done

echo "All 50 users attempted bookings."