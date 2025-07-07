# WSO2 API Manager - Custom Workflow Extension

A custom workflow extension for WSO2 API Manager that triggers GitHub Actions workflows when APIs are promoted through the lifecycle. This extension integrates with the API Manager's lifecycle management system to automatically trigger CI/CD pipelines when APIs transition to "Promoted" state.

## Overview

This project implements a custom workflow executor that extends `APIStateChangeSimpleWorkflowExecutor` to intercept API lifecycle state transitions. When an API is promoted from "Published" to "Promoted" state, the workflow executor triggers a GitHub Actions workflow using the GitHub API.

## Features

- **Lifecycle Integration**: Hooks into WSO2 API Manager's lifecycle transitions
- **GitHub Actions Integration**: Automatically triggers GitHub workflows on API promotion
- **Configurable Parameters**: Supports configuration of GitHub repository, workflow file, and authentication
- **Error Handling**: Comprehensive error handling with logging for troubleshooting

## Technology Stack

- **Java 1.8** with Maven build system
- **WSO2 API Manager 4.x (this was tested with 4.5)
- **JUnit 4.11** for testing
- **Apache HTTP Client** for GitHub API integration

## Project Structure

```
lifecycle_demo/
├── lifecycle/                      # Main Maven project
│   ├── src/main/java/
│   │   └── com/example/wso2/
│   │       └── PromoteWorkflowExecutor.java
│   ├── src/test/java/
│   └── pom.xml
├── workflow_definition/            # WSO2 configuration files
│   └── workflow_extensions_template.xml
├── lifecycle.json                  # API lifecycle definition
└── README.md
```

## Build and Installation

### Prerequisites

- Java 1.8 or higher
- Maven 3.6+
- WSO2 API Manager 4.x (this was tested with 4.5)
- Access to GitHub repository for workflow triggers

### Building the Project

All build commands should be executed from the `lifecycle/` directory:

```bash
cd lifecycle/

# Compile the project
mvn clean compile

# Run tests
mvn test

# Create JAR package
mvn package

# Full build and install
mvn clean install
```

The build creates: `target/promote-workflows-1.0-SNAPSHOT.jar`

## Configuration

### 1. GitHub Configuration

Connection to Github requires the following information:

- `githubToken`: GitHub Personal Access Token with `repo` and `actions` scopes
- `githubOwner`: GitHub username or organization name
- `githubRepo`: Repository name containing the workflow
- `workflowFileName`: Name of the GitHub Actions workflow file (e.g., `deploy.yml`)

### 2. WSO2 API Manager Registry Configuration

1. Copy the JAR file to WSO2 API Manager:
   ```bash
   cp lifecycle/target/promote-workflows-1.0-SNAPSHOT.jar <APIM_HOME>/repository/components/lib/
   ```

2. Configure workflows through the WSO2 registry:

   a. **Access Management Console:**
      - Navigate to `https://<Server Host>:9443/carbon`
      - Sign in with administrator credentials

   b. **Navigate to Workflow Extensions:**
      - Go to **Resources** → **Browse**
      - Navigate to `/_system/governance/apimgt/applicationdata/workflow-extensions.xml`

   c. **Configure Custom Workflow:**
      - Click **Edit as text**
      - Replace the default `APIStateChange` configuration with:

      ```xml
      <APIStateChange executor="com.example.wso2.PromoteWorkflowExecutor">
         <Property name="githubToken">YOUR_GITHUB_TOKEN</Property>
         <Property name="githubOwner">YOUR_GITHUB_USERNAME</Property>
         <Property name="githubRepo">YOUR_REPOSITORY_NAME</Property>
         <Property name="workflowFileName">YOUR_WORKFLOW_FILE.yml</Property>
      </APIStateChange>
      ```

   d. **Save and Restart:**
      - Save the configuration
      - Restart the WSO2 API Manager server

### 3. Lifecycle Configuration

The project includes a custom lifecycle configuration in `lifecycle.json` that defines the API states and transitions:

- **Created** → **Pre-Released** → **Published** → **Promoted** → **Deprecated** → **Retired**

The workflow executor triggers when transitioning from **Published** to **Promoted** state.

You need to follow instructions here to configure the lifecycle: https://apim.docs.wso2.com/en/latest/manage-apis/design/lifecycle-management/customize-api-life-cycle/

*Note*: the file as reference is in JSON format to help format/edit, the outer {} must be removed before you paste it in the UI.

### 4. GitHub Actions Workflow

Create a GitHub Actions workflow file in your repository (e.g., `.github/workflows/api-promote.yml`):

```yaml
name: API Promotion Workflow

on:
  workflow_dispatch:
    inputs:
      apiName:
        description: 'Name of the API being promoted'
        required: true
      apiVersion:
        description: 'Version of the API being promoted'
        required: true

jobs:
  promote:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Process API Promotion
        run: |
          echo "Processing promotion for API: ${{ github.event.inputs.apiName }}"
          echo "API Version: ${{ github.event.inputs.apiVersion }}"
          # Add your custom promotion logic here
```

## Usage

1. **Deploy the Extension:**
   - Build and deploy the JAR file to WSO2 API Manager
   - Configure the workflow extension in the registry

2. **Create an API:**
   - Use the API Manager Publisher to create a new API
   - Progress through the lifecycle states: Created → Pre-Released → Published

3. **Promote the API:**
   - In the API Publisher, click **Lifecycle** tab
   - Click **Promote** button
   - The custom workflow executor will trigger the GitHub Actions workflow

4. **Monitor Execution:**
   - Check WSO2 API Manager logs for workflow execution details
   - Monitor GitHub Actions workflow execution in your repository

## Troubleshooting

### Common Issues

1. **JAR not found:**
   - Ensure the JAR file is in `<APIM_HOME>/repository/components/lib/`
   - Restart the WSO2 API Manager server

2. **GitHub API authentication failed:**
   - Verify the GitHub token has correct permissions (`repo`, `actions`)
   - Check that the token is not expired

3. **Workflow not triggered:**
   - Verify the workflow extension configuration in the registry
   - Check that the executor class name is correct: `com.example.wso2.PromoteWorkflowExecutor`

4. **GitHub workflow not found:**
   - Ensure the workflow file exists in `.github/workflows/` directory
   - Verify the `workflowFileName` property matches the actual file name

### Logs

Monitor the following log files for debugging:
- `<APIM_HOME>/repository/logs/wso2carbon.log`
- `<APIM_HOME>/repository/logs/http_access.log`

Look for log entries from `com.example.wso2.PromoteWorkflowExecutor` class.

## Security Considerations

- **Token Security**: Store GitHub tokens securely and rotate them regularly
- **Network Security**: Ensure WSO2 API Manager can reach GitHub API (https://api.github.com)
- **Access Control**: Use principle of least privilege for GitHub token permissions

## Development

### Adding New Workflow Types

To extend the workflow executor for other lifecycle transitions:

1. Modify the condition in `PromoteWorkflowExecutor.java:47`
2. Add additional logic for different `lcAction` values
3. Rebuild and redeploy the JAR file

### Testing

Run unit tests:
```bash
cd lifecycle/
mvn test
```

Add integration tests in `src/test/java/com/example/` directory.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For issues related to:
- **WSO2 API Manager**: Consult [WSO2 Documentation](https://apim.docs.wso2.com/)
- **GitHub Actions**: Check [GitHub Actions Documentation](https://docs.github.com/en/actions)
- **This Extension**: Create an issue in this repository