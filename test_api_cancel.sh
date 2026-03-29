#!/bin/bash

# Script to randomly cancel bookings and verify waitlist promotion

# Assume app is running on http://localhost:8080

for i in {1..50}
do
    EMAIL="testuser$i@example.com"
    echo "Canceling for user $i: $EMAIL"

    # 1. Login
    echo "Logging in..."
    TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -u $EMAIL:password)
    echo "Token: $TOKEN"

    echo -e "\n"

    # 2. Get user bookings
    echo "Getting user bookings..."
    BOOKINGS_JSON=$(curl -s -X GET http://localhost:8080/bookings/user \
      -H "Authorization: Bearer $TOKEN")
    echo "Bookings: $BOOKINGS_JSON"

    # Extract booking IDs using jq
    BOOKING_IDS=$(echo $BOOKINGS_JSON | jq -r '.[].id')
    # If no bookings, skip
    if [ -z "$BOOKING_IDS" ] || [ "$BOOKING_IDS" == "null" ]; then
        echo "No bookings for user $i"
        echo -e "\n\n"
        continue
    fi

    # Pick a random booking ID to cancel
    RANDOM_BOOKING_ID=$(echo "$BOOKING_IDS" | shuf -n 1)
    echo "Randomly selected booking ID to cancel: $RANDOM_BOOKING_ID"

    # 3. Cancel the booking
    echo "Canceling booking $RANDOM_BOOKING_ID..."
    curl -X POST http://localhost:8080/bookings/cancel/$RANDOM_BOOKING_ID \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 4. Get user bookings after cancel
    echo "Getting user bookings after cancel..."
    curl -X GET http://localhost:8080/bookings/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    echo "User $i canceled a booking."
    echo -e "\n\n"
done

echo "All users attempted cancellations."