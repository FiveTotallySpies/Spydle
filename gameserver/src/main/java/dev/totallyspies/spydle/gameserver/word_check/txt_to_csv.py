input_file = "words.txt"  # Replace with your input file name
output_file = "words.csv"  # Replace with your desired output file name

# Read the input file and process lines
with open(input_file, "r") as infile:
    lines = infile.readlines()  # Read all lines into a list

# Open the output file to write
with open(output_file, "w") as outfile:
    for i, line in enumerate(lines):
        words = line.replace('"', "").strip().split()  # Split words in the line by spaces
        csv_line = ",".join(words)   # Join words with commas
        outfile.write(csv_line)      # Write the line to the output file
        
        if i < len(lines) - 1:       # Add a comma between lines (except the last)
            outfile.write(",")       # Append a comma without a newline

print(f"CSV file has been written to {output_file}.")

