#include<bits/stdc++.h>
using namespace std;

class Book {
private:
    string title;
    string author;
    double price;
    double discount;

    void clearStream() {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
    }

public:
    // Constructor
    Book() : title("N/A"), author("N/A"), price(0.0), discount(0.0) {}

    void inputData() {
        while (true) {
            cout << "Enter title: ";
            getline(cin, title);
            if (!title.empty()) {
                break;
            }
            cout << "Title cannot be empty. Try again." << endl;
        }

        while (true) {
            cout << "Enter author name: ";
            getline(cin, author);
            if (!author.empty()) {
                break;
            }
            cout << "Author name cannot be empty. Try again." << endl;
        }

        while (true) {
            cout << "Enter price: ";
            if (cin >> price && price >= 0) {
                clearStream();
                break;
            } else {
                cout << "Invalid price. Please enter a positive number." << endl;
                clearStream();
            }
        }

        while (true) {
            cout << "Enter discount percentage: ";
            if (cin >> discount && discount >= 0 && discount <= 100) {
                clearStream();
                break;
            } else {
                cout << "Invalid discount. Please enter a number between 0 and 100." << endl;
                clearStream();
            }
        }
    }
    
    void displayData() const {
        cout << "\n+-------------------------+" << endl;
        cout << "|   --- Book Details ---  |" << endl;
        cout << "+-------------------------+" << endl;
        cout << "| Title:  " << left << setw(15) << title << " |" << endl;
        cout << "| Author: " << left << setw(15) << author << " |" << endl;
        cout << "| Price:  $" << left << setw(14) << fixed << setprecision(2) << price << " |" << endl;
        cout << "+-------------------------+" << endl;
    }
    
};

int main() {
    Book b;
    b.inputData();



    b.displayData();
    return 0;
}
