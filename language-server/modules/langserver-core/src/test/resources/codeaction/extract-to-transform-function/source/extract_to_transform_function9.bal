type Address record {|
    string houseNo;
    string line1;
    string line2;
    string city;
    string country;
|};

type Employee record {|
    string name;
    string empId;
    string email;
    Address address;
|};

type Person record {|
    string name;
    string email;
    Address address;
|};

type Admission record {
    string empId;
    string admissionDate;
};

function transform(Person person, Admission admission) returns Employee => {
    name: person.name,
    empId: admission.empId,
    email: person.email,
    address: retAddress(person.address.city, person.address.country)
};

function retAddress(string city, string country) returns Address => {
    houseNo: "123",
    line1: "line1",
    line2: "line2",
    city: "city",
    country: "country"
};
