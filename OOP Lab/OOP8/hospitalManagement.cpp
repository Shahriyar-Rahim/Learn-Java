#include <iostream>
using namespace std;

class Patient;

class Doctor
{
public:
    bool checkPatient(Patient p);
    void advice()
    {
        cout << "Take rest and drink water, bari jao" << endl;
    }
};

class Patient
{
    string name;
    int age;
    float temparature;

public:
    Patient(string n = "Unk", int a = 0, double t = 0.0) : name(n), age(a), temparature(t) {}

    void showInfo()
    {
        cout << "\n cxPatient Name: " << name << endl;
        cout << "Age: " << age << endl;
    }

    friend void emergrncyAlert(Patient p);
    friend bool Doctor::checkPatient(Patient p);
};

bool Doctor::checkPatient(Patient p)
{
    cout << "\nChecking Temparature: " << p.temparature << " C" << endl;

    if(p.temparature > 100)
    {
        cout << "Jor eseche, basa jao" << endl;
        return true;
    }
    else
    {
        cout << "Jor aseni, asbeeeeeeeeeee!??o_oo_oo_oo_oo_oo_o" << endl;
        return false;
    }
}

void emergrncyAlert(Patient p)
{
    if(p.temparature > 103)
        cout << "Hoaspital jaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaao" << endl;
    else
        cout << "Basay thako, asbeeeeeeeeeee!??o_oo_oo_oo_oo_oo_o" << endl;
}

int main()
{
    Patient p1("Rahim", 22, 105.98);
    Doctor d;
    p1.showInfo();
    if(d.checkPatient(p1))
    {
        d.advice();
        emergrncyAlert(p1);
    }

    Patient p2("Mantasa", 22, 99.98);
    Doctor d1;
    p2.showInfo();
    if(d1.checkPatient(p2))
    {
        d1.advice();
        emergrncyAlert(p2);
    }
}
