#include<bits/stdc++.h>
#include <iostream>
#include <limits> // Required for numeric_limits
#include <string>
#include <stdexcept> // Required for exceptions
#include <iomanip> // Required for formatting
#include <vector> // Required for vector


using namespace std;

class Student {
private:
    int id;
    string name;
    float cgpa;

    // Helper function to validate numerical input
    template<typename T>
    T getValidInput(const string& prompt) {
        T value;
        while (true) {
            cout << prompt;
            if (!(cin >> value)) {
                cout << "Invalid input. Please enter a valid number." << endl;
                cin.clear(); // Clear error flags
                // Ignore the rest of the line to prevent an infinite loop
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
            } else {
                cin.ignore(numeric_limits<streamsize>::max(), '\n'); // Clear buffer after successful numeric input
                return value;
            }
        }
    }

public:
    // Constructor
    Student() : id(0), name("N/A"), cgpa(0.0f) {}

    // Function to input student data with validation
    void inputData() {
        // Input ID with validation for non-negative numbers
        while (true) {
            id = getValidInput<int>("Enter your ID (non-negative integer): ");
            if (id >= 0) {
                break;
            }
            cout << "ID cannot be negative. Please try again." << endl;
        }

        // Input Name
        cout << "Enter your name: ";
        getline(cin, name);
        if (name.empty()) {
            name = "N/A";
        }

        // Input CGPA with validation (0.0 to 4.0)
        while (true) {
            cgpa = getValidInput<float>("Enter your CGPA (0.0 - 4.0): ");
            if (cgpa >= 0.0f && cgpa <= 4.0f) {
                break;
            }
            cout << "CGPA must be between 0.0 and 4.0. Please try again." << endl;
        }
    }

    // Function to display student data
     void displayData() const {
        cout << "\n+-------------------------+" << endl;
        cout << "| --- Student Details --- |" << endl;
        cout << "+-------------------------+" << endl;
        cout << "| Name: " << left << setw(16) << name << "|" << endl;
        cout << "| ID:   " << left << setw(16) << id << "|" << endl;
        cout << "| CGPA: " << left << setw(16) << fixed << setprecision(2) << cgpa << "|" << endl;
        cout << "+-------------------------+" << endl;
    }
};

int main() {
    // handling for runtime errors
    try {
        Student s1;
        s1.inputData();
        s1.displayData();
    } catch (const exception& e) {
        cerr << "An error occurred: " << e.what() << endl;
        return 1;
    }

    return 0;
}

