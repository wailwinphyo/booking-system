#!/bin/bash

# Script to test booking system APIs with multiple users

# Assume app is running on http://localhost:8080

for i in {1..50}
do
    EMAIL="testuser$i@example.com"
    echo "Testing user $i: $EMAIL"

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

    # 3. Login with Basic Auth
    echo "Logging in..."
    TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -u $EMAIL:password)
    echo "Token: $TOKEN"

    echo -e "\n"

    # 4. Get packages
    echo "Getting packages..."
    curl -X GET http://localhost:8080/packages \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 5. Get packages by country
    echo "Getting Singapore packages..."
    curl -X GET http://localhost:8080/packages/country/Singapore \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 6. Purchase a package (vary by user: 1=SG Basic, 2=SG Premium, 3=MM Basic, 4=MM Premium)
    MOD=$((i % 4))
    if [ $MOD -eq 1 ]; then
        PACKAGE_ID=1  # SG Basic
        COUNTRY="Singapore"
        CLASS_ID=1  # Yoga SG
    elif [ $MOD -eq 2 ]; then
        PACKAGE_ID=2  # SG Premium
        COUNTRY="Singapore"
        CLASS_ID=1  # Yoga SG
    elif [ $MOD -eq 3 ]; then
        PACKAGE_ID=3  # MM Basic
        COUNTRY="Myanmar"
        CLASS_ID=5  # Yoga MM
    else
        PACKAGE_ID=4  # MM Premium
        COUNTRY="Myanmar"
        CLASS_ID=5  # Yoga MM
    fi
    echo "Purchasing package $PACKAGE_ID for $COUNTRY..."
    curl -X POST "http://localhost:8080/packages/purchase?email=$EMAIL&packageId=$PACKAGE_ID&cardDetails=1234567890123456" \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 7. Get user packages
    echo "Getting user packages..."
    curl -X GET http://localhost:8080/packages/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 8. Get class schedules for the country and pick a random class to book
    echo "Getting $COUNTRY class schedules..."
    SCHEDULES_JSON=$(curl -s -X GET http://localhost:8080/bookings/classes/$COUNTRY \
      -H "Authorization: Bearer $TOKEN")
    echo "Schedules: $SCHEDULES_JSON"

    # Extract class IDs using jq (assuming jq is installed)
    CLASS_IDS=$(echo $SCHEDULES_JSON | jq -r '.[].id')
    # Pick a random ID from the list
    RANDOM_CLASS_ID=$(echo "$CLASS_IDS" | shuf -n 1)
    echo "Randomly selected class ID: $RANDOM_CLASS_ID"

    echo -e "\n"

    # 9. Book multiple times to deplete credits
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

    # 10. Get user packages after bookings
    echo "Getting user packages after all bookings..."
    curl -X GET http://localhost:8080/packages/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    # 11. Get user bookings
    echo "Getting user bookings..."
    curl -X GET http://localhost:8080/bookings/user \
      -H "Authorization: Bearer $TOKEN"

    echo -e "\n"

    echo "User $i test complete."
    echo -e "\n\n"
done

echo "All 50 users tested."