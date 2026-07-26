#include<iostream>
using namespace std;

class Test
{
    int id;
public:
    Test(int x) : id(x)
    {
        cout << "Constructor called for: " << id << endl;
    }
    ~Test()
    {
        cout << "Destructor called for: " << id << endl;
    }
};

int main()
{
    Test T1(1);
    {
        {
            Test t2(2);
        }
        Test t3(3);
    }

    return 0;
}
