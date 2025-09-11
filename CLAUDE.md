# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Scavenger is a runtime dead code analysis tool for Java applications. It consists of multiple components:
- **Scavenger Agent**: Java agent that instruments applications to track method invocations
- **Scavenger Collector**: Collects and stores invocation data from agents
- **Scavenger API**: REST API for accessing collected data 
- **Scavenger Frontend**: Vue.js web interface for visualizing dead code analysis
- **Scavenger Python Agent**: Beta Python version of the agent

The system helps identify unused code ("dead code") by tracking actual runtime method invocations across JVM-based applications.

## Build System & Commands

### Primary Build Commands
```bash
# Build entire project
./gradlew build

# Build specific modules
./gradlew assemble -p scavenger-collector
./gradlew assemble -p scavenger-api
./gradlew assemble -p scavenger-agent-java

# Run tests
./gradlew test                    # All tests
./gradlew check                   # Tests + code quality checks
./gradlew integrationTest         # Integration tests (agent module)
./gradlew :scavenger-agent-java:integrationTest  # Java version compatibility tests

# Clean build artifacts
./gradlew clean
```

### Frontend Development
```bash
# Frontend is built with Vite/npm (in scavenger-frontend/)
cd scavenger-frontend
npm install                       # Install dependencies
npm run build                     # Production build
npm run watch                     # Development watch mode

# Or use Gradle tasks:
./gradlew :scavenger-frontend:vite     # Build frontend
./gradlew :scavenger-frontend:watch    # Watch mode
```

### Code Quality
- **Kotlin linting**: Uses ktlint plugin (`./gradlew ktlintCheck`)
- **Java agent**: Lombok for boilerplate code generation
- **Test frameworks**: JUnit 5, Mockito, AssertJ, RestAssured

## Architecture Overview

### Multi-Module Structure
The project follows a clean architecture with separate concerns:

- **scavenger-model**: Core data models and protobuf definitions
- **scavenger-entity**: JPA/database entities 
- **scavenger-schema**: Database schema and migrations (Liquibase)
- **scavenger-agent-java**: Bytecode instrumentation agent (ByteBuddy + ASM)
- **scavenger-collector**: Spring Boot gRPC/HTTP server for data collection
- **scavenger-api**: Spring Boot REST API + Thymeleaf for web interface
- **scavenger-frontend**: Vue.js 3 SPA with Vite build system

### Technology Stack

**Backend (Kotlin/Java)**:
- **Framework**: Spring Boot 3.2.4, Spring Data JDBC
- **Database**: MySQL, H2 (development), Vitess support
- **Communication**: gRPC (agent↔collector), REST (API)
- **Build**: Gradle with Kotlin DSL, Java 24 toolchain
- **Agent**: ByteBuddy 1.17.7 for runtime instrumentation, ASM 9.8 for bytecode manipulation
- **Java Support**: Agent runs on Java 8+ (tested: 8, 11, 17, 21, 24), builds with Java 24

**Frontend (JavaScript)**:
- **Framework**: Vue.js 3 with Composition API
- **Build**: Vite 3.x
- **UI Library**: Element Plus
- **State**: Pinia store
- **Routing**: Vue Router 4
- **i18n**: Vue i18n for internationalization

### Key Architectural Patterns

**Agent Instrumentation Flow**:
1. `ScavengerAgent` (premain) → `CodeBaseScanner` → `ByteBuddy` transformation
2. Runtime method tracking via `InvocationRegistry` and `CallStackTracker`
3. Periodic publishing to collector via gRPC (`Publisher` + `GrpcClient`)

**Data Collection Flow**:
1. Agent sends codebase + invocation data to Collector
2. Collector stores in database with garbage collection
3. API queries collector database for analysis
4. Frontend displays dead code analysis results

**Database Design**:
- Codebase fingerprinting for version tracking
- Method signature-based identification
- Invocation tracking with call stack support
- Multi-environment and multi-application support

## Development Guidelines

### Module Dependencies
- Models/entities are shared across backend modules
- Agent is standalone with minimal dependencies
- API depends on collector for data access patterns
- Frontend build is integrated into API static resources

### Testing Strategy
- **Unit tests**: Standard JUnit 5 with Mockito
- **Integration tests**: Uses test-sets plugin for agent testing
- **Java Compatibility**: Multi-version testing across Java 8, 11, 17, 21, 24
- **Agent testing**: Includes JMH benchmarks, bytecode manipulation, and multi-JVM testing
- **API testing**: RestAssured for REST API integration tests
- **CI/CD**: GitHub Actions with matrix testing for Java version compatibility

### Configuration Management
- **Agent**: `scavenger.conf` file-based configuration
- **Spring components**: Standard application.properties with profiles
- **Frontend**: Vite configuration with environment-specific builds
- **Database**: Liquibase migrations for schema management

### Key Configuration Files
- `scavenger-agent-java/src/main/resources/internal.properties` - Agent version info
- `scavenger-collector/src/main/resources/application*.properties` - Collector config
- `scavenger-api/src/main/resources/application*.properties` - API config
- `scavenger-frontend/package.json` - Frontend dependencies and scripts

### Performance Considerations
- Agent uses shadow JAR with relocation to avoid conflicts
- Collector uses Armeria for high-performance gRPC/HTTP
- Frontend bundles are optimized with Vite
- Database queries are optimized for large codebases

## CI/CD & GitHub Actions

### Automated Workflows
- **Build & Test**: Automated testing on PR to `main`/`develop` branches
- **Java Compatibility**: Matrix testing across Java 8, 11, 17, 21, 24
- **Release**: Automatic release creation on version tags
- **Multi-version Integration**: Comprehensive agent testing across supported Java versions

### Java Version Support
- **Build Requirements**: Java 24 (for latest features and tooling)
- **Runtime Compatibility**: Java 8+ (maintains backward compatibility)
- **Tested Versions**: 8, 11, 17, 21, 24 (automated CI testing)
- **Agent Compatibility**: Bytecode manipulation works across all supported versions