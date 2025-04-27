# QServer Transaction Log Analyzer - Compilation Guide

## Overview

This document provides instructions for compiling and running the QServer Transaction Log Analyzer application. The analyzer is designed to process QServer transaction log files, identify performance bottlenecks, and generate comprehensive reports with visualizations.

## Prerequisites

Before compiling the application, ensure you have the following installed:

- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6.0 or higher
- At least 2GB of free RAM for processing large log files

## Project Structure

The project follows standard Maven directory structure:

```
transaction-log-analyzer/
├── pom.xml                                  # Maven project configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qserver/loganalyzer/     # Java source files
│   │   └── resources/
│   │       └── logback.xml                  # Logging configuration
│   └── test/
│       └── java/
│           └── com/qserver/loganalyzer/     # Test source files
└── README.md                                # Project documentation
```

## Compilation Steps

### 1. Clone or Download the Project

If you received the project as a ZIP file, extract it to a directory of your choice. If using Git:

```bash
git clone <repository-url> transaction-log-analyzer
cd transaction-log-analyzer
```

### 2. Compile with Maven

The project uses Maven for dependency management and build automation. To compile the project:

```bash
mvn clean compile
```

This command will:
- Clean any previous build artifacts
- Download all required dependencies
- Compile the Java source code

### 3. Run Tests (Optional)

To ensure everything is working correctly, you can run the tests:

```bash
mvn test
```

### 4. Package the Application

To create an executable JAR file with all dependencies included:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory named `transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## Running the Application

After compilation, you can run the application using the following command:

```bash
java -jar target/transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <log_directory> [output_directory]
```

Where:
- `<log_directory>` is the path to the directory containing QServer transaction log files (CSV format)
- `[output_directory]` is an optional path for output reports (default: 'output')

Example:
```bash
java -jar target/transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/logs /path/to/reports
```

## Importing into IntelliJ IDEA

To import the project into IntelliJ IDEA:

1. Open IntelliJ IDEA
2. Select "Import Project" from the welcome screen (or File > New > Project from Existing Sources)
3. Navigate to the project directory and select the `pom.xml` file
4. Click "Open as Project"
5. In the import dialog, ensure "Import Maven projects automatically" is checked
6. Click "Next" and follow the prompts to complete the import

## Customizing the Build

### Memory Settings

If you're processing large log files, you may need to increase the JVM memory allocation:

```bash
java -Xmx4g -jar target/transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <log_directory> [output_directory]
```

This allocates 4GB of heap memory to the JVM.

### Logging Configuration

The application uses SLF4J with Logback for logging. You can customize the logging behavior by modifying the `src/main/resources/logback.xml` file before compilation.

## Troubleshooting

### Common Issues

1. **Compilation Errors**
   - Ensure you're using JDK 11 or higher
   - Verify that Maven is correctly installed
   - Check for network issues that might prevent downloading dependencies

2. **Runtime Errors**
   - Ensure the log directory exists and contains valid QServer transaction log files
   - Check that you have sufficient permissions to read from the log directory and write to the output directory
   - Increase memory allocation if processing large log files

3. **Report Generation Issues**
   - Ensure you have write permissions for the output directory
   - Check that the log files are in the expected CSV format

### Getting Help

If you encounter issues not covered in this guide, please contact the development team with:
- The exact command you ran
- The complete error message or stack trace
- The version of Java and Maven you're using
- Sample log files (if possible)

## Advanced Configuration

The application supports configuration through a properties file. Create a file named `config.properties` in the same directory as the JAR file with the following options:

```properties
# Number of top transactions to include in reports
topTransactions=10

# Number of top transaction kinds to include in reports
topKinds=5

# Number of top threads to include in reports
topThreads=5

# Chart dimensions
chartWidth=800
chartHeight=600

# Report formats (comma-separated)
reportFormat=docx,xlsx
```

Then run the application with:

```bash
java -jar target/transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <log_directory> [output_directory]
```

The application will automatically detect and use the configuration file.
