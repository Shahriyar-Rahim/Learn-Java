#include <iostream>
#include <string> // Added for string handling
#include <limits> // Added for clearing the input buffer

using namespace std;

class BankAccount {
    string name;
    double balance;

public:
    // Constructor
    BankAccount(double x = 0) : balance(x), name("Unknown") {
        // Only print if it's a significant initialization
        if (x != 0) cout << "Initial Balance: " << balance << endl;
    }

    // Overloaded Operators
    BankAccount operator+(const BankAccount& obj);
    BankAccount operator-(const BankAccount& ob);

    void bname() {
        cout << "Enter Full Name: ";
        // Use ws to skip any leading whitespace/newlines from previous inputs
        getline(cin >> ws, name);
    }

    void display() {
        cout << "----------------------" << endl;
        cout << "Account Holder: " << name << endl;
        cout << "Current Balance: " << balance << endl;
    }
};

// Redefined to copy the name to the temporary object
BankAccount BankAccount::operator+(const BankAccount& obj) {
    BankAccount t;
    t.balance = this->balance + obj.balance;
    t.name = this->name; // Preserve the name
    return t;
}

// Redefined to copy the name to the temporary object
BankAccount BankAccount::operator-(const BankAccount& ob) {
    BankAccount t;
    t.balance = this->balance - ob.balance;
    t.name = this->name; // Preserve the name
    return t;
}

int main() {
    BankAccount b1, b2;

    b1.bname();
    b1 = b1 + 500; // The name is now preserved

    b2.bname();
    b2 = b2 - 500;

    b1.display();
    b2.display();

    return 0;
}
