# 🚀 Enterprise gRPC Test Automation Framework

![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)
![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![gRPC](https://img.shields.io/badge/gRPC-1.60+-blue.svg)

> A production-ready, enterprise-grade automation platform for testing **gRPC microservices** with resilience testing, contract validation, and rich reporting.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [Framework Components](#-framework-components)
- [Writing Tests](#-writing-tests)
- [Configuration](#-configuration)
- [Running Tests](#-running-tests)
- [Reporting](#-reporting)
- [CI/CD Integration](#-cicd-integration)
- [Advanced Features](#-advanced-features)
- [Best Practices](#-best-practices)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

---

## 🎯 Overview

This framework provides a **complete enterprise solution** to test gRPC services at scale.

It supports:
- Functional validation
- Contract compatibility
- Resilience testing
- Chaos engineering
- Performance assertions
- Streaming RPC verification

Designed for **production microservices**, not demo projects.

---

## ✨ Key Features

### 🏗️ Architecture
- SOLID design principles
- Thread-safe execution
- Connection pooling
- Generic reusable clients
- Full observability

### 🧪 Testing
- Functional tests
- Negative tests
- Contract validation
- Performance assertions
- Streaming RPC testing
- Chaos testing

### 🔧 Enterprise Ready
- Multi-environment support
- Parallel TestNG execution
- CI/CD ready
- Allure reporting
- Mock server support
- Data-driven tests

### 🛡️ Reliability
- Retry with exponential backoff
- Circuit breaker protection
- Deadline enforcement
- Network failure simulation

---

## 🏛️ Architecture
- Tests → Assertions → Service Clients → Interceptors → Channel → gRPC Server
- mvn allure:serve
- @Test
public void myFirstGrpcTest() {

    GetCampaignRequest request = CampaignRequestBuilder
        .forGetCampaign()
        .withPhoneNumber("+1234567890")
        .withCampaignId("SUMMER_SALE_2024")
        .build();

    GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

    CampaignAssertions.assertThat(response)
        .isSuccess()
        .hasTitle("Summer Sale")
        .hasLatencyLessThan(500);
}
- config/
 ├── dev.properties
 ├── stage.properties
 └── prod.properties
- grpc.host=localhost
grpc.port=9090
grpc.deadline.seconds=30
grpc.retry.max-attempts=3
- public class CampaignServiceClient extends BaseGrpcClient<CampaignServiceBlockingStub> {

    public GrpcResponse<GetCampaignResponse> getCampaign(GetCampaignRequest request) {
        return executeWithRetry(() -> getStub().getCampaign(request));
    }
}
- CampaignAssertions.assertThat(response)
    .isSuccess()
    .hasStatusCode(Status.Code.OK)
    .hasTitle("Summer Sale")
    .hasActiveStatus();
- @Test(groups = {"functional", "smoke"})
public void testGetCampaignSuccess() {

    GetCampaignRequest request = CampaignRequestBuilder.forGetCampaign()
        .withPhoneNumber("+1234567890")
        .withCampaignId("SUMMER_SALE_2024")
        .build();

    GrpcResponse<GetCampaignResponse> response = campaignClient.getCampaign(request);

    CampaignAssertions.assertThat(response)
        .isSuccess()
        .hasActiveStatus();
}
- mvn test
mvn test -DsuiteXmlFile=testng-smoke.xml
mvn test -DsuiteXmlFile=testng-full.xml
ENV=stage mvn test



| Layer | Responsibility |
|------|---------------|
| Test Layer | Test implementation |
| Assertion Engine | Validation DSL |
| Service Client | gRPC interaction |
| Interceptors | Auth, retry, logging |
| Infrastructure | Channel & stub creation |
| Config | Multi-environment config |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- gRPC proto files

### Install

```bash
git clone <repo>
cd grpc-test-framework
mvn clean compile
mvn test
