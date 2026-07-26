#include<iostream>
using namespace std;

class Employee
{
public:
    virtual void calculateSalary() = 0;
    virtual void display() = 0;

    ~Employee()
    {
        cout << "Destructor called" << endl;
    }
};

class FTEmployee : public Employee
{
private:
    double dailySalary;
public:
    FTEmployee(double x) : dailySalary(x) {}
    void calculateSalary() override
    {
        dailySalary *= 30;
    }

    void display() override
    {
        cout << "Salary of the full time Emp: " << dailySalary << endl;
    }
};

class PTEmployee : public Employee
{
private:
    double hourlyWage, hoursWorked;
public:
    PTEmployee(double x, double y) : hourlyWage(x), hoursWorked(y) {}
    void calculateSalary() override
    {
        hourlyWage *= hoursWorked;
    }

    void display() override
    {
        cout << "Salary of the part time Emp: " << hourlyWage << endl;
    }
};

int main()
{
    Employee* empl;

    FTEmployee e(100);
    empl = &e;
    empl -> calculateSalary();
    empl -> display();

    empl = new PTEmployee(10, 10);
    empl -> calculateSalary();
    empl -> display();

    delete empl;

    return 0;
}
