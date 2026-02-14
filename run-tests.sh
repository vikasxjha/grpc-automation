#!/bin/bash

# gRPC Test Automation - Run Script
# Helper script for executing tests with various configurations

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== gRPC Test Automation Framework ===${NC}\n"

# Function to show usage
show_usage() {
    echo "Usage: ./run-tests.sh [OPTION]"
    echo ""
    echo "Options:"
    echo "  compile         Compile and generate proto stubs"
    echo "  test            Run all tests"
    echo "  smoke           Run smoke tests only"
    echo "  functional      Run functional tests only"
    echo "  negative        Run negative tests only"
    echo "  contract        Run contract tests only"
    echo "  streaming       Run streaming tests only"
    echo "  resiliency      Run resiliency tests only"
    echo "  performance     Run performance tests only"
    echo "  report          Generate and open Allure report"
    echo "  clean           Clean build artifacts"
    echo "  help            Show this help message"
    echo ""
    echo "Environment variables:"
    echo "  ENV             Set environment (dev, stage, prod) - default: dev"
    echo "  GRPC_HOST       Override gRPC host"
    echo "  GRPC_PORT       Override gRPC port"
    echo ""
    echo "Examples:"
    echo "  ./run-tests.sh compile"
    echo "  ./run-tests.sh smoke"
    echo "  ENV=stage ./run-tests.sh test"
    echo "  GRPC_HOST=localhost GRPC_PORT=9090 ./run-tests.sh test"
}

# Function to compile and generate stubs
compile() {
    echo -e "${YELLOW}Compiling and generating proto stubs...${NC}"
    mvn clean compile
    echo -e "${GREEN}✓ Compilation successful${NC}\n"
}

# Function to run tests
run_tests() {
    local test_suite=$1
    local suite_name=$2

    echo -e "${YELLOW}Running ${suite_name}...${NC}"

    # Build Maven command
    CMD="mvn test"

    # Add suite file if specified
    if [ -n "$test_suite" ]; then
        CMD="$CMD -DsuiteXmlFile=src/test/resources/${test_suite}.xml"
    fi

    # Add environment
    if [ -n "$ENV" ]; then
        CMD="$CMD -Denv=$ENV"
        echo -e "${YELLOW}Environment: $ENV${NC}"
    fi

    # Add custom host/port
    if [ -n "$GRPC_HOST" ]; then
        CMD="$CMD -Dgrpc.host=$GRPC_HOST"
        echo -e "${YELLOW}gRPC Host: $GRPC_HOST${NC}"
    fi

    if [ -n "$GRPC_PORT" ]; then
        CMD="$CMD -Dgrpc.port=$GRPC_PORT"
        echo -e "${YELLOW}gRPC Port: $GRPC_PORT${NC}"
    fi

    echo ""
    $CMD

    echo -e "\n${GREEN}✓ Tests completed${NC}\n"
}

# Function to generate Allure report
generate_report() {
    echo -e "${YELLOW}Generating Allure report...${NC}"
    mvn allure:serve
}

# Function to clean build
clean_build() {
    echo -e "${YELLOW}Cleaning build artifacts...${NC}"
    mvn clean
    echo -e "${GREEN}✓ Clean successful${NC}\n"
}

# Main script logic
case "${1}" in
    compile)
        compile
        ;;
    test)
        run_tests "testng" "All Tests"
        ;;
    smoke)
        run_tests "testng-smoke" "Smoke Tests"
        ;;
    functional)
        run_tests "" "Functional Tests"
        mvn test -Dtest=CampaignFunctionalTests
        ;;
    negative)
        mvn test -Dtest=CampaignNegativeTests
        ;;
    contract)
        mvn test -Dtest=CampaignContractTests
        ;;
    streaming)
        mvn test -Dtest=CampaignStreamingTests
        ;;
    resiliency)
        mvn test -Dtest=CampaignResiliencyTests
        ;;
    performance)
        mvn test -Dtest=CampaignPerformanceTests
        ;;
    report)
        generate_report
        ;;
    clean)
        clean_build
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo -e "${RED}Error: Unknown option '${1}'${NC}\n"
        show_usage
        exit 1
        ;;
esac

