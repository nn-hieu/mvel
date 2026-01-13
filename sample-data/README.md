# Sample Data
This directory contains sample data files to help you quickly test and use the application.

## Included Files
Currently, this folder includes:
- **`organizations-100000.zip`**  
  A ZIP archive containing a CSV file with **100,000 records**.  
  This dataset is suitable for quick testing and local development.
- **`organizations-data-type.json`**  
  A JSON file that defines the data type for each column in the CSV file.

## Large Sample Data
If you need sample data with a **larger volume of records** (for performance testing or stress testing), the large dataset is **not included in this repository** to keep the repository lightweight.

### Option 1: Download directly
You can download the large sample dataset from GitHub Releases:
https://github.com/nn-hieu/mvel/releases/download/sample-data/sample-data-large-size.zip

### Option 2: Download using script (Recommended)
A helper script is provided to automatically download and extract the large dataset.

Please run the following scripts in the right order (from the **root folder** of the project `mvel`):
```bash
chmod +x scripts/download-sample-data-large-size.sh
```
```bash
./scripts/download-sample-data-large-size.sh
```
The script will:
- Download the large sample dataset
- Extract it into the `sample-data/` directory
- Overwrite existing files if necessary

## 📌 Notes
- The large dataset may require more memory and processing time.
- Use it only for performance testing or benchmarking.
- Make sure you have `curl` or `wget` installed before running the script.