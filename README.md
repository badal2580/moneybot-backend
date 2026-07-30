# MoneyBot Pro — Backend

A secure and production-ready cryptocurrency paper-trading backend built with **Java**, **Spring Boot**, **Spring Security**, **JWT**, **Spring Data JPA**, and **MySQL**.

The backend powers the MoneyBot frontend by handling authentication, trade execution, portfolio calculations, analytics, scheduled price updates, and Telegram notifications.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Render](https://img.shields.io/badge/Deployed%20on-Render-46E3B7?logo=render&logoColor=black)](https://moneybot-backend.onrender.com)

## Live Links

**Backend API:**  
https://moneybot-backend.onrender.com

**Frontend Application:**  
https://moneybot-frontend-s5zo.vercel.app/

**Frontend Repository:**  
https://github.com/badal2580/moneybot-frontend

> MoneyBot is a simulated paper-trading project created for educational and portfolio purposes. It does not execute real-money cryptocurrency trades.

## Core Features

### Authentication and Security

- User registration
- Secure login
- JWT token generation
- JWT request validation
- BCrypt password encryption
- Spring Security filter chain
- Protected API endpoints
- User-specific trade access
- Unauthorized request handling
- CORS configuration for local and deployed frontend

### Trading

- Buy simulated cryptocurrency trades
- Sell open trades
- Multi-coin support
- Live Coinbase price integration
- Target price
- Stop-loss price
- Open trades
- Closed trades
- User-specific trade ownership
- Automatic current-price updates
- Realized and unrealized profit calculation

### Portfolio

- Total investment
- Current portfolio value
- Unrealized profit/loss
- Realized profit/loss
- ROI percentage
- Open trade count
- Closed trade count

### Analytics

- Total trades
- Winning trades
- Losing trades
- Win rate
- Best trade
- Worst trade
- Average profit
- Average loss
- Trade profit chart data
- Cumulative profit chart data

### Automation

- Scheduled price refresh
- Target-price monitoring
- Stop-loss monitoring
- Automatic trade status updates
- Telegram trade notifications

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- JSON Web Token
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Lombok
- Validation API
- OpenAPI / Swagger
- Coinbase API
- Telegram Bot API
- Docker
- Render
- Railway

## Architecture

```text
React Frontend
      │
      ▼
REST Controllers
      │
      ▼
Spring Security + JWT Filter
      │
      ▼
Service Layer
      │
      ├── Authentication Service
      ├── Trade Service
      ├── Telegram Service
      └── Scheduler
      │
      ▼
Spring Data JPA Repositories
      │
      ▼
MySQL Database
```

## Project Structure

```text
src/main/java/com/badal/moneybot/
├── client/
│   └── CoinbaseClient.java
├── config/
│   ├── CorsConfig.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   └── TradeController.java
├── dto/
│   ├── ApiResponse.java
│   ├── AuthResponse.java
│   ├── BuyTradeRequest.java
│   ├── DashboardResponse.java
│   ├── LoginRequest.java
│   ├── PortfolioSummaryResponse.java
│   ├── ProfitChartResponse.java
│   ├── RegisterRequest.java
│   └── TradeAnalyticsResponse.java
├── entity/
│   ├── Trade.java
│   └── User.java
├── repository/
│   ├── TradeRepository.java
│   └── UserRepository.java
├── scheduler/
│   └── AutoTradingScheduler.java
├── security/
│   ├── JwtFilter.java
│   └── JwtService.java
├── service/
│   ├── AuthService.java
│   ├── TelegramService.java
│   └── TradeService.java
└── MoneyBotApplication.java
```

## API Endpoints

### Authentication

```http
POST /auth/register
POST /auth/login
```

### Trading

```http
POST /trade/buy
PUT  /trade/sell/{id}
GET  /trade/open
GET  /trade/closed
GET  /trade/history
GET  /trade/search
```

### Dashboard and Analytics

```http
GET /trade/dashboard
GET /trade/portfolio-summary
GET /trade/analytics
GET /trade/statistics
GET /trade/profit-chart
```

## Authentication Example

### Register

```http
POST /auth/register
Content-Type: application/json
```

```json
{
  "name": "Badal Solanki",
  "email": "badal@example.com",
  "password": "SecurePassword123"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "email": "badal@example.com",
  "password": "SecurePassword123"
}
```

Example response:

```json
{
  "token": "jwt-token",
  "message": "Login successful"
}
```

## Buy Trade Example

```http
POST /trade/buy
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

```json
{
  "symbol": "BTC-USD",
  "quantity": 0.001,
  "targetPrice": 70000,
  "stopLoss": 60000
}
```

## Supported Trading Pairs

```text
BTC-USD
ETH-USD
SOL-USD
XRP-USD
DOGE-USD
```

## Run Locally

Clone the repository:

```bash
git clone https://github.com/badal2580/moneybot-backend.git
```

Move into the project:

```bash
cd moneybot-backend
```

Create a MySQL database:

```sql
CREATE DATABASE moneybot;
```

Set the required environment variables:

```bash
export DB_URL=jdbc:mysql://localhost:3306/moneybot
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your_long_secure_secret
export TELEGRAM_BOT_TOKEN=your_telegram_bot_token
export TELEGRAM_CHAT_ID=your_telegram_chat_id
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will run at:

```text
http://localhost:8081
```

## Environment Variables

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
TELEGRAM_BOT_TOKEN
TELEGRAM_CHAT_ID
PORT
```

Example `application.properties` configuration:

```properties
spring.application.name=MoneyBot

server.port=${PORT:8081}

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

coinbase.base-url=https://api.coinbase.com

telegram.bot.token=${TELEGRAM_BOT_TOKEN}
telegram.chat.id=${TELEGRAM_CHAT_ID}
```

## Swagger Documentation

After starting the backend, open:

```text
http://localhost:8081/swagger-ui/index.html
```

Use the Authorize button and enter:

```text
Bearer YOUR_JWT_TOKEN
```

## Security Design

- Passwords are stored using BCrypt hashing.
- JWT is validated before protected requests reach controllers.
- Trades are fetched using the authenticated user's email.
- Users cannot sell or access another user's trades.
- Password and user entity data are excluded from trade JSON responses.
- Secrets are provided using environment variables.
- CORS is restricted to the approved frontend URLs.

## Deployment

The backend is deployed using:

- Render
- Railway

Production environment variables must be configured in the deployment dashboard.

The application uses:

```properties
server.port=${PORT:8081}
```

so the hosting platform can provide its own port.

## Database Model

### User

```text
id
name
email
password
role
```

### Trade

```text
id
user_id
symbol
buy_price
sell_price
current_price
quantity
profit
status
created_at
target_price
stop_loss
```

## Future Improvements

- Refresh-token authentication
- Email verification
- Forgot-password flow
- WebSocket live prices
- Redis caching
- Rate limiting
- Unit testing
- Integration testing
- Docker Compose
- CI/CD workflow
- Audit logs
- More trading pairs
- PostgreSQL production support

## Related Repository

**MoneyBot Frontend:**  
https://github.com/badal2580/moneybot-frontend

## Author

**Badal Solanki**

Java Backend Developer and Computer Science Engineering Student

GitHub:  
https://github.com/badal2580

## Disclaimer

This project is made for learning, demonstration, and portfolio purposes only. It does not provide financial advice and does not execute real-money cryptocurrency transactions.

---

If you find this project useful, consider giving the repository a star.
