# Elasticsearch API Documentation for Postman Testing

This document provides information about the Elasticsearch search endpoints available in the review-service. These endpoints can be used for testing in Postman.

## Base URL

All endpoints are prefixed with: `/api/reviews`

## Elasticsearch Search Endpoints

### 1. Search Reviews by Keyword

Searches across multiple fields (content, username, productName) for the given keyword.

- **URL**: `/search`
- **Method**: GET
- **Parameters**:
  - `keyword` (required): The search term
  - `page` (optional, default=0): Page number for pagination
  - `size` (optional, default=5): Number of results per page
- **Example Request**:
  ```
  GET /api/reviews/search?keyword=great&page=0&size=10
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": [
      {
        "id": "1",
        "userId": 123,
        "username": "user123",
        "productId": 456,
        "productName": "Sample Product",
        "rating": 5,
        "content": "This product is great!",
        "createdAt": "2023-10-15T10:30:00"
      }
    ],
    "hasMore": false,
    "totalElements": 1,
    "totalPages": 1,
    "message": "Tìm kiếm theo từ khóa: great"
  }
  ```

### 2. Get Reviews by User ID

Retrieves all reviews created by a specific user.

- **URL**: `/user/{userId}`
- **Method**: GET
- **Path Variables**:
  - `userId` (required): ID of the user
- **Parameters**:
  - `page` (optional, default=0): Page number for pagination
  - `size` (optional, default=5): Number of results per page
- **Example Request**:
  ```
  GET /api/reviews/user/123?page=0&size=10
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": [
      {
        "id": "1",
        "userId": 123,
        "username": "user123",
        "productId": 456,
        "productName": "Sample Product",
        "rating": 5,
        "content": "This product is great!",
        "createdAt": "2023-10-15T10:30:00"
      }
    ],
    "hasMore": false,
    "totalElements": 1,
    "totalPages": 1
  }
  ```

### 3. Get Reviews by Exact Rating

Retrieves all reviews with a specific rating value.

- **URL**: `/rating/{rating}`
- **Method**: GET
- **Path Variables**:
  - `rating` (required): The exact rating value (1-5)
- **Parameters**:
  - `page` (optional, default=0): Page number for pagination
  - `size` (optional, default=5): Number of results per page
- **Example Request**:
  ```
  GET /api/reviews/rating/5?page=0&size=10
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": [
      {
        "id": "1",
        "userId": 123,
        "username": "user123",
        "productId": 456,
        "productName": "Sample Product",
        "rating": 5,
        "content": "This product is great!",
        "createdAt": "2023-10-15T10:30:00"
      }
    ],
    "hasMore": false,
    "totalElements": 1,
    "totalPages": 1,
    "message": "Đánh giá với rating: 5"
  }
  ```

### 4. Get Reviews by Minimum Rating

Retrieves all reviews with a rating greater than or equal to the specified value.

- **URL**: `/rating/min/{rating}`
- **Method**: GET
- **Path Variables**:
  - `rating` (required): The minimum rating value (1-5)
- **Parameters**:
  - `page` (optional, default=0): Page number for pagination
  - `size` (optional, default=5): Number of results per page
- **Example Request**:
  ```
  GET /api/reviews/rating/min/4?page=0&size=10
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": [
      {
        "id": "1",
        "userId": 123,
        "username": "user123",
        "productId": 456,
        "productName": "Sample Product",
        "rating": 5,
        "content": "This product is great!",
        "createdAt": "2023-10-15T10:30:00"
      },
      {
        "id": "2",
        "userId": 124,
        "username": "user124",
        "productId": 456,
        "productName": "Sample Product",
        "rating": 4,
        "content": "Good product overall",
        "createdAt": "2023-10-16T11:45:00"
      }
    ],
    "hasMore": false,
    "totalElements": 2,
    "totalPages": 1,
    "message": "Đánh giá với rating tối thiểu: 4"
  }
  ```

## Direct Elasticsearch Test APIs

These endpoints are specifically designed for testing the Elasticsearch functionality directly. They are available under the `/api/es/test` base path.

### 1. Get Elasticsearch Info

Returns basic information about the Elasticsearch repository.

- **URL**: `/api/es/test/info`
- **Method**: GET
- **Example Request**:
  ```
  GET /api/es/test/info
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "message": "Elasticsearch test controller is active",
    "count": 42
  }
  ```

### 2. Get All Reviews

Retrieves all reviews with pagination and sorting options.

- **URL**: `/api/es/test/all`
- **Method**: GET
- **Parameters**:
  - `page` (optional, default=0): Page number for pagination
  - `size` (optional, default=10): Number of results per page
  - `sortBy` (optional, default="createdAt"): Field to sort by
  - `sortDir` (optional, default="desc"): Sort direction ("asc" or "desc")
- **Example Request**:
  ```
  GET /api/es/test/all?page=0&size=10&sortBy=rating&sortDir=desc
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": [
      {
        "id": "1_1",
        "userId": 1,
        "username": "user1",
        "productId": 1,
        "productName": "Product 1",
        "rating": 5,
        "content": "Great product!",
        "createdAt": "2023-11-15T10:30:00"
      }
    ],
    "totalElements": 42,
    "totalPages": 5,
    "currentPage": 0
  }
  ```

### 3. Get Review by ID

Retrieves a specific review by its ID.

- **URL**: `/api/es/test/{id}`
- **Method**: GET
- **Path Variables**:
  - `id` (required): The ID of the review (typically in format "userId_productId")
- **Example Request**:
  ```
  GET /api/es/test/1_1
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": {
      "id": "1_1",
      "userId": 1,
      "username": "user1",
      "productId": 1,
      "productName": "Product 1",
      "rating": 5,
      "content": "Great product!",
      "createdAt": "2023-11-15T10:30:00"
    },
    "message": "Review found"
  }
  ```
- **Not Found Response**:
  ```json
  {
    "status": 200,
    "message": "Review not found"
  }
  ```

### 4. Create Review

Creates a new review directly in Elasticsearch.

- **URL**: `/api/es/test/create`
- **Method**: POST
- **Headers**:
  - `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "userId": 1,
    "productId": 2,
    "username": "testuser",
    "productName": "Test Product",
    "content": "This is a test review for Elasticsearch",
    "rating": 5,
    "createdAt": "2023-11-15T10:30:00"
  }
  ```
- **Example Request**:
  ```
  POST /api/es/test/create
  ```
- **Success Response**:
  ```json
  {
    "status": 201,
    "data": {
      "id": "1_2",
      "userId": 1,
      "username": "testuser",
      "productId": 2,
      "productName": "Test Product",
      "rating": 5,
      "content": "This is a test review for Elasticsearch",
      "createdAt": "2023-11-15T10:30:00"
    },
    "message": "Review created successfully"
  }
  ```

### 5. Update Review

Updates an existing review in Elasticsearch.

- **URL**: `/api/es/test/{id}`
- **Method**: PUT
- **Path Variables**:
  - `id` (required): The ID of the review to update
- **Headers**:
  - `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "userId": 1,
    "productId": 2,
    "username": "testuser",
    "productName": "Test Product",
    "content": "This content has been updated",
    "rating": 4,
    "createdAt": "2023-11-15T10:30:00"
  }
  ```
- **Example Request**:
  ```
  PUT /api/es/test/1_2
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "data": {
      "id": "1_2",
      "userId": 1,
      "username": "testuser",
      "productId": 2,
      "productName": "Test Product",
      "rating": 4,
      "content": "This content has been updated",
      "createdAt": "2023-11-15T10:30:00"
    },
    "message": "Review updated successfully"
  }
  ```
- **Not Found Response**:
  ```json
  {
    "status": 404,
    "message": "Review not found"
  }
  ```

### 6. Delete Review

Deletes a review from Elasticsearch.

- **URL**: `/api/es/test/{id}`
- **Method**: DELETE
- **Path Variables**:
  - `id` (required): The ID of the review to delete
- **Example Request**:
  ```
  DELETE /api/es/test/1_2
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "message": "Review deleted successfully"
  }
  ```
- **Not Found Response**:
  ```json
  {
    "status": 404,
    "message": "Review not found"
  }
  ```

### 7. Reindex All Reviews

Triggers a reindex operation for all reviews (placeholder implementation).

- **URL**: `/api/es/test/reindex`
- **Method**: POST
- **Example Request**:
  ```
  POST /api/es/test/reindex
  ```
- **Success Response**:
  ```json
  {
    "status": 200,
    "message": "Reindex operation would be performed here",
    "currentIndexSize": 42
  }
  ```

## Testing in Postman

1. Import the collection into Postman
2. Set up an environment variable for the base URL (e.g., `{{base_url}}` = `http://localhost:8085`)
3. Test each endpoint with different parameters
4. Verify that the Elasticsearch functionality is working correctly

## Notes

- These endpoints require a running Elasticsearch instance configured as per the application settings
- If Elasticsearch is unavailable, the service may fall back to standard database queries
- For performance reasons, some queries only work efficiently with Elasticsearch and may be limited or unavailable when Elasticsearch is down
- The direct test APIs (`/api/es/test/*`) are intended for testing and debugging only and should not be used in production
