# Calculator App

A basic Spring Boot calculator REST API (add, subtract, multiply, divide).

## Run
    mvn spring-boot:run

## Endpoints
    GET /api/calculator/add?a=2&b=3
    GET /api/calculator/subtract?a=10&b=4
    GET /api/calculator/multiply?a=6&b=7
    GET /api/calculator/divide?a=10&b=4

Each returns `{"result": <number>}`; dividing by zero returns HTTP 400 with `{"error": "Division by zero"}`.

## Test
    mvn test
