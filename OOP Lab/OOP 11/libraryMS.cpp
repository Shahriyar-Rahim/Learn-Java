#include<iostream>
#include<vector>
#include<stdexcept>
using namespace std;

class Library;

class Book
{
    string title;
    int id;
    int quantity;

public:
    Book(string s,int i,int q) : title(s), id(i), quantity(q) {}

    void search(int x)
    {
        if(id==x)
            display();
        else
            cout<<"Opps! Not Found of the id: "<< x <<endl;
    }
    void search(string x)
    {
        if(title==x)
            display();
        else
            cout<<"Opps! Not Found of the title: "<< x <<endl;
    }
    void operator+(const Book& b)
    {
        if(id==b.id)
            quantity +=b.quantity;
    }
    void operator+(int x)
    {
        quantity +=x;
    }
    void operator-(const Book& b)
    {
        if(id==b.id && quantity <= b.quantity)
            quantity -=b.quantity;
    }
    void operator-(int x)
    {
        quantity -=x;
    }
    virtual void display()
    {
        cout<<"Book Title : "<<title<<endl;
        cout<<"Book ID : "<<id<<endl;
        cout<<"Available Copies : "<<quantity<<endl;
    }
    friend class Library;
};

class Ebook:private Book
{
    double fileSize;

public:
    Ebook(string s,int i,int q,double f):Book(s,i,q), fileSize(f) {}
    void display()override
    {
        Book::display();
        cout<<"File size (in MB) : "<<fileSize<<endl;
    }
};

class Library
{
    vector <Book> books;
public:
    void addBook(Book b)
    {
        books.push_back(b);
    }
    void displayBooks()
    {
        for(int i=0; i<books.size(); i++)
        {
            books[i].display();
        }
    }
    void borrowBook(int id)
    {
        for(int i=0; i<books.size(); i++)
        {
            if(id==books[i].id)
            {
                if(books[i].quantity<=0)
                {
                    throw runtime_error("Books not available");
                }
                books[i]-1;
                cout<<"Book successfully borrowed! "<<endl;
                return;
            }
        }
        throw runtime_error("Invalid Book ID! ");
    }
    void returnBook(int id)
    {
        for(int i=0; i<books.size(); i++)
        {
            if(id==books[i].id)
            {
                books[i]+1;
                cout<<"Book returned successfully ! "<<endl;
                return;
            }
        }
        throw runtime_error("Invalid Book ID! ");
    }
};

int main()
{
    Library lib;

    Book b1("C++",101,5);
    Book b2("OOP",102,3);

    lib.addBook(b1);
    lib.addBook(b2);

    cout<<"\nAll Books : \n";
    lib.displayBooks();

    cout<<"\nSearch By ID : \n";
    b1.search(101);

    cout<<"\nSearch By TITLE : \n";
    b1.search("OOP");
    cout<<endl;

    try
    {

        lib.borrowBook(101);
        lib.returnBook(101);
        lib.borrowBook(999);
        lib.returnBook(555);
    }

    catch(exception &e)
    {
        cout<<"\nException : "<<e.what()<<endl;

    }

    cout<<"\n Ebook Information : \n";
    Ebook eb1("AI Basics",201,10,15.5);
    eb1.display();

    return 0;
}
