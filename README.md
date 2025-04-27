# QServer Transaction Log Analyzer

## Overview

The QServer Transaction Log Analyzer is a Java application designed to analyze QServer transaction logs to identify performance bottlenecks, CPU-intensive transactions, and transactions causing wait times for other operations. The tool provides comprehensive reports and visualizations to help diagnose and resolve performance issues in QServer applications.

## Features

- **Transaction Log Parsing**: Efficiently parses QServer transaction log files in CSV format
- **Performance Analysis**: Identifies CPU-intensive transactions, high wait times, and resource utilization patterns
- **Root Cause Detection**: Determines which transactions are causing others to wait
- **Comprehensive Reporting**: Generates detailed reports in both Word and Excel formats
- **Data Visualization**: Creates charts to visualize performance patterns
- **Thread Analysis**: Analyzes thread utilization and identifies thread contention issues

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher
- QServer transaction log files in CSV format

### Installation

1. Clone or download this repository
2. Build the project using Maven:

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

### Usage

Run the analyzer with:

```bash
java -jar target/transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <log_directory> [output_directory]
```

Where:
- `<log_directory>` is the path to the directory containing QServer transaction log files
- `[output_directory]` is an optional path for output reports (default: 'output')

For detailed compilation and usage instructions, see the [Compilation Guide](Compilation_Guide.md).

## Output

The analyzer generates the following outputs:

1. **Word Report**: A comprehensive analysis report with findings and recommendations
2. **Excel Spreadsheet**: Detailed transaction data for further analysis
3. **Charts**: Visualizations of performance patterns
4. **Console Summary**: A brief summary of key findings

## Configuration

You can customize the analyzer behavior by creating a `config.properties` file in the same directory as the JAR file. Available configuration options:

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

## Project Structure

```
transaction-log-analyzer/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qserver/loganalyzer/
│   │   │       ├── model/          # Data models
│   │   │       ├── parser/         # Log file parsers
│   │   │       ├── analyzer/       # Performance analysis logic
│   │   │       ├── report/         # Report generation
│   │   │       ├── util/           # Utility classes
│   │   │       └── TransactionLogAnalyzerApp.java  # Main application
│   │   └── resources/
│   │       └── logback.xml         # Logging configuration
│   └── test/
│       └── java/                   # Test classes
├── pom.xml                         # Maven configuration
├── README.md                       # This file
└── Compilation_Guide.md            # Detailed compilation instructions
```

## Troubleshooting

If you encounter issues:

1. Ensure your log files are in the correct CSV format
2. Check that you have sufficient permissions to read input files and write to the output directory
3. For large log files, increase JVM memory with `-Xmx` option:
   ```bash
   java -Xmx4g -jar transaction-log-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <log_directory>
   ```
4. Review the application logs in the `logs` directory

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Developed for analyzing QServer application performance
- Uses Apache POI for document generation
- Uses JFreeChart for data visualization
