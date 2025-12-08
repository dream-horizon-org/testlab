<div align="center">

  <h1>TestLab</h1>

  <p><strong>Enterprise-grade A/B Testing & Feature Experimentation Platform</strong></p>

</div>

## 🌟 Overview

TestLab is a robust, high-performance experimentation platform designed for modern applications. It provides comprehensive A/B testing capabilities with flexible targeting rules, real-time user allocation, and variant management - giving you complete control over your feature experiments.

## Why TestLab?

* 🧪 **Powerful Experimentation**: Full A/B testing with multi-variant support and statistical rigor
* 🎯 **Flexible Targeting**: Rule-based targeting with cohorts, attributes, and custom conditions
* 🏢 **Multi-Tenant Ready**: Supports multiple projects with logical isolation
* 🚀 **Quick Implementation**: Get experiments running in minutes
* ⚡ **High Performance**: Built on Vert.x for reactive, non-blocking I/O with Aerospike for lightning-fast allocations
* 📊 **Real-time Allocation**: Instant user assignment with consistent bucketing
* 🔄 **Lifecycle Management**: Full experiment lifecycle from DRAFT to CONCLUDED

## 📋 Table of Contents

* [Features](#-features)
* [Architecture](#-architecture)
* [Getting Started](#-getting-started)
* [Configuration](#-configuration)
* [API Reference](#-api-reference)
* [Deployment](#-deployment)
* [Contributing](#-contributing)
* [License](#-license)

## ✨ Features

### Experiment Management

* **📝 Create Experiments**: Define A/B tests with multiple variants
* **🔄 Lifecycle States**: DRAFT → LIVE → PAUSED → CONCLUDED/TERMINATED
* **📊 Variant Weights**: Configure traffic distribution across variants
* **🏷️ Tags & Owners**: Organize experiments with tags and assign ownership
* **📈 Metrics Tracking**: Define primary and secondary metrics for analysis

### Targeting & Allocation

* **👥 Cohort-based Targeting**: Target specific user cohorts
* **📋 Rule-based Targeting**: Define complex targeting rules with conditions
  * Attribute-based conditions (operand, operator, value)
  * Multiple data types: STRING, INTEGER, DOUBLE, BOOLEAN, LIST, SEMVER
  * Rich operators: EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, CONTAINS, IN, etc.
* **🎲 Distribution Strategies**: RANDOM allocation with configurable exposure
* **🔒 Threshold Limits**: Control maximum experiment participation

### Real-time Allocation

* **🚀 Fast User Assignment**: Sub-millisecond allocation decisions
* **🔄 Consistent Bucketing**: Users always see the same variant
* **📱 Multi-experiment Support**: Allocate users to multiple experiments simultaneously
* **🔀 Reallocation Support**: Move users between variants when needed

### Administration

* **📜 Experiment History**: Track all changes with audit logs
* **🔍 Filter & Search**: Find experiments by status, type, name, tags, or owners
* **✅ Key Availability**: Check experiment key uniqueness before creation

## 🏗️ Architecture

TestLab is built with a modern, scalable architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                      TestLab API                            │
│                    (Vert.x REST API)                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Experiment │  │  Allocation │  │   Admin Service     │ │
│  │   Service   │  │   Service   │  │  (History, Tags)    │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│  ┌─────────────────────┐    ┌─────────────────────────────┐│
│  │     PostgreSQL      │    │        Aerospike            ││
│  │  (Experiment Data)  │    │  (User Allocations/Counts)  ││
│  └─────────────────────┘    └─────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites

* **Docker** ≥ 20.10 ([Download Docker Desktop](https://www.docker.com/products/docker-desktop/))
* **Docker Compose** ≥ 2.0 ([Install instructions](https://docs.docker.com/compose/install/))
* **Maven** ≥ 3.6 ([Download Maven](https://maven.apache.org/download.cgi))
* **Java 17** (JDK) ([Download Java 17](https://www.oracle.com/java/technologies/downloads/#java17))

### Verify Installations

```bash
docker --version
mvn --version
java -version
```

**Important**: Ensure that Java 17 is the active version. Maven should also be configured to use Java 17 - verify with `mvn --version`.

### Port Requirements

Ensure the following ports are available:

* `8142` – PostgreSQL database
* `8130` – Aerospike
* `8100` – TestLab application server

### Quick Start

1. **Clone the repository**:
```bash
git clone https://github.com/dream-horizon-org/testlab.git
cd testlab
```

2. **Start infrastructure services**:
```bash
# With default project key
docker-compose up -d

# Or with a custom project key
PROJECT_KEY=your-project-key-here docker-compose up -d
```

> **Note**: The `PROJECT_KEY` environment variable is used to create PostgreSQL partitions and seed data for your project. If not specified, it defaults to `550e8400-e29b-41d4-a716-446655440001`.

3. **Build and run TestLab**:
```bash
mvn clean package -DskipTests
cd target/testlab
PG_USER=postgres PG_PASSWORD=postgres java -Dapp.environment=local -Dlogback.configurationFile=./resources/logback/logback.xml -jar testlab-1.0-fat.jar
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PG_USER` | PostgreSQL username | - |
| `PG_PASSWORD` | PostgreSQL password | - |
| `PROJECT_KEY` | Project identifier for multi-tenant partitioning | `550e8400-e29b-41d4-a716-446655440001` |
| `app.environment` | Environment type (local, test, prod) | - |

### Configuration Files

Configuration files are located in `src/main/resources/config/`:

* `application/default.conf` - Application settings
* `postgresql/default.conf` - Database configuration
* `aerospike/default.conf` - Aerospike cache settings
* `http-server/default.conf` - HTTP server settings
* `circuit-breaker/default.conf` - Resilience settings

### Multi-Tenant Setup (Project Key)

TestLab uses PostgreSQL partitioning for multi-tenant isolation. Each project has its own partition:

```bash
# Start with a specific project
PROJECT_KEY=my-project-key docker-compose up -d

# Or set it in a .env file
echo "PROJECT_KEY=my-project-uuid" > .env
docker-compose up -d
```

When making API calls, include the project key in the header:
```bash
curl -X GET http://localhost:8100/v1/experiments \
  -H "x-project-key: my-project-key"
```

> **Note**: If you try to access a project that doesn't have a partition, you'll receive a `PARTITION_NOT_FOUND` error. Ensure partitions are created before making API calls.

## 📚 API Reference

### Swagger Specification
The complete API specification is available at:
* [Swagger File](./src/main/resources/webroot/swagger/swagger.yaml)

### Database Schema
* [DB Schema](./src/main/resources/db/postgresql/schema.sql)

## 🚀 Deployment

### Running with Docker

```bash
# Default project key
docker-compose up -d

# Custom project key (creates partitions for your project)
PROJECT_KEY=my-custom-project-id docker-compose up -d
```

The `PROJECT_KEY` variable:
- Creates PostgreSQL partitions for the specified project
- Seeds initial experiment data associated with that project
- Enables multi-tenant isolation per project

### Running the JAR

```bash
mvn clean package
cd target/testlab
PG_USER=<pg-username> \
PG_PASSWORD=<pg-password> \
java -Dapp.environment=<env-type> \
     -Dlogback.configurationFile=./resources/logback/logback.xml \
     -jar testlab-1.0-fat.jar
```

### IntelliJ Run Configuration

1. Create IntelliJ Run Configuration of type `Application`
2. Add VM options:
   * `-Dapp.environment=<env-type>`
   * `-Dlogback.configurationFile=logback/logback-local.xml`
3. Set program arguments: `run verticle.com.ascend.testlab.MainVerticle`
4. Set environment variables:
   ```
   ENV=<env-type>
   PG_USER=<pg-username>
   PG_PASSWORD=<pg-password>
   ```

## 🛠️ Development

### Code Formatting

Auto-format code using Spotify's fmt plugin:
```bash
mvn com.spotify.fmt:fmt-maven-plugin:format
```

### Running Tests

```bash
# Unit tests
mvn test

# All tests including integration
mvn verify
```

### Generate API Documentation

```bash
mvn javadoc:javadoc
```

Documentation will be generated in `docs/api-v1.0/`.

## 🤝 Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `mvn verify`
5. Submit a pull request

### Coding Standards

* Follow existing code style
* Add tests for new functionality
* Update documentation as needed

## 📄 License

TestLab is licensed under the [MIT License](./LICENSE).

---

Built with ❤️ by the TestLab team and contributors
