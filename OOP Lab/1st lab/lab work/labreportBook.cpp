#include <iostream>
#include <string>
#include <vector>
#include <iomanip>
#include <limits>
using namespace std;

class Book
{
private:
    string title;
    string author;
    double price;
    double discount; // This stores the price after discount

    static void clearInputBuffer()
    {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
    }

public:
    Book() : title("N/A"), author("N/A"), price(0.0), discount(0.0) {}

    string getTitle() const { return title; }

    void inputData()
    {
        cout << "Enter book title: ";
        getline(cin, title);
        cout << "Enter book author: ";
        getline(cin, author);

        while (true)
        {
            cout << "Enter book price: ";
            if (cin >> price && price >= 0)
            {
                clearInputBuffer();
                break;
            }
            else
            {
                cout << "Invalid input. Please enter a non-negative number for price." << endl;
                clearInputBuffer();
            }
        }
    }

    bool applyDiscount(double discountPercentage)
    {
        if (discountPercentage < 0 || discountPercentage > 100)
        {
            cout << "Invalid discount percentage. It should be between 0 and 100." << endl;
            return false; 
        }
        double discountAmount = price * (discountPercentage / 100);
        discount = price - discountAmount;
        cout << "Discount of " << discountPercentage << "% applied to '" << title << "'." << endl;
        return true;
    }

    void displayData() const
    {
        cout << "\n+--------------------------+" << endl;
        cout << "|    --- Book Details ---   |" << endl;
        cout << "+--------------------------+" << endl;
        cout << "| Title:  " << left << setw(16) << title << " |" << endl;
        cout << "| Author: " << left << setw(16) << author << " |" << endl;
        cout << "| Price:  $" << left << setw(15) << fixed << setprecision(2) << price << " |" << endl;
        if(discount > 0) {
            cout << "| Discounted: $" << left << setw(12) << fixed << setprecision(2) << discount << " |" << endl;
        }
        cout << "+--------------------------+" << endl;
    }
};

int main()
{
    int numBooks;
    cout << "Enter the number of books: ";
    if (!(cin >> numBooks)) return 0;
    cin.ignore();

    vector<Book> library(numBooks);
    for (int i = 0; i < numBooks; ++i)
    {
        cout << "\n--- Enter details for book " << i + 1 << " ---" << endl;
        library[i].inputData();
    }

    char isWantDiscount;
    cout << "\nDo you want to apply a discount to any book? (y/n): ";
    cin >> isWantDiscount;

    if (isWantDiscount == 'y' || isWantDiscount == 'Y')
    {
        cout << "\nAvailable Books:" << endl;
        for (int i = 0; i < numBooks; ++i)
        {
            cout << (i + 1) << ". " << library[i].getTitle() << endl;
        }

        int bookIndex;
        while (true) {
            cout << "\nEnter the number (1-" << numBooks << ") of the book: ";
            if (cin >> bookIndex && bookIndex >= 1 && bookIndex <= numBooks) break;
            cout << "Invalid index. Try again.";
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
        }

        double discountPercent;
        while (true) 
        {
            cout << "Enter discount percentage (0-100): ";
            if (cin >> discountPercent) 
            {
                if (library[bookIndex - 1].applyDiscount(discountPercent)) {
                    break;
                }
            } 
            else 
            {
                cout << "Invalid input. Please enter a number." << endl;
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
            }
        }
    }

    cout << "\n--- Final Inventory Status ---" << endl;
    for (int i = 0; i < numBooks; ++i)
    {
        library[i].displayData();
    }

    return 0;
}