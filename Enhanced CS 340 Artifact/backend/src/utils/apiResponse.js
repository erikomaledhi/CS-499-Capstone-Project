/**
 * Standard API response format
 */
class ApiResponse {
  constructor(success, message, data = null, statusCode = 200) {
    this.success = success;
    this.message = message;
    if (data !== null) {
      this.data = data;
    }
    this.statusCode = statusCode;
    this.timestamp = new Date().toISOString();
  }

  static success(message, data = null, statusCode = 200) {
    return new ApiResponse(true, message, data, statusCode);
  }

  static error(message, statusCode = 500) {
    return new ApiResponse(false, message, null, statusCode);
  }

  static created(message, data) {
    return new ApiResponse(true, message, data, 201);
  }

  static noContent(message = 'Operation successful') {
    return new ApiResponse(true, message, null, 204);
  }
}

module.exports = ApiResponse;
