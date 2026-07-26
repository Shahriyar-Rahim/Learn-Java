#include<iostream>
using namespace std;

class BankAccount
{
    string name;
    double balance;

public:
    BankAccount(double x = 0): balance(x) {
        cout << "Main Balance: " << balance << endl;
    }
    BankAccount operator+(const BankAccount obj);
    BankAccount operator-(const BankAccount ob);
    void bname()
    {
        cout << "Enter Name: ";
        getline(cin >> ws, name);
    }
    void display();

};

BankAccount BankAccount:: operator+(const BankAccount obj)
{
    BankAccount t;
    t.balance = balance + obj.balance;
    t.name = this->name;

    return t;
}

BankAccount BankAccount::operator-(const BankAccount ob)
{
    BankAccount t;
    t.balance = balance - ob.balance;
    t.name = this->name;

    return t;
}

void BankAccount:: display()
{
    cout << "Name: " << name << endl;
    cout << "After Adding Balance: " << balance << endl;
}

int main()
{
    BankAccount b1, b2;

    b1.bname();
    b2.bname();
    b1 = b1 + 500;
    b2 = b1 - 500;

    b1.display();
    b2.display();

    return 0;
}
