import ballerina/http;
import ballerina/lang.'string as strings;

listener http:MockListener testEP = new(9090);

type Person record {|
    string name;
    int age;
|};

type Stock record {|
    int id;
    float price;
|};

service echo on testEP {

    resource function body1(http:Caller caller, http:Request req, @http:BodyParam string person) {
        json responseJson = { "Person": person };
        checkpanic caller->respond(<@untainted json> responseJson);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/body2/{key}"
    }
    resource function body2(http:Caller caller, http:Request req, @http:PathParam string key,
                            @http:BodyParam string person) {
        json responseJson = { Key: key, Person: person };
        checkpanic caller->respond(<@untainted json> responseJson);
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"]
    }
    resource function body3(http:Caller caller, http:Request req, @http:BodyParam json person) {
        json|error val1 = person.name;
        json|error val2 = person.team;
        json name = val1 is json ? val1 : ();
        json team = val2 is json ? val2 : ();
        checkpanic caller->respond(<@untainted> { Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function body4(http:Caller caller, http:Request req, @http:BodyParam xml person) {
        string name = <@untainted string> person.getElementName();
        string team = <@untainted string> person.getTextValue();
        checkpanic caller->respond({ Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function body5(http:Caller caller, http:Request req, @http:BodyParam byte[] person) {
        http:Response res = new;
        var name = <@untainted> strings:fromBytes(person);
        if (name is string) {
            res.setJsonPayload({ Key: name });
        } else {
            res.setTextPayload("Error occurred while byte array to string conversion");
            res.statusCode = 500;
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function body6(http:Caller caller, http:Request req, @http:BodyParam Person person) {
        string name = <@untainted string> person.name;
        int age = <@untainted int> person.age;
        checkpanic caller->respond({ Key: name, Age: age });
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function body7(http:Caller caller, http:Request req, @http:BodyParam Stock person) {
        checkpanic caller->respond();
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function body8(http:Caller caller, http:Request req, @http:BodyParam Person[] persons) {
        var jsonPayload = typedesc<json>.constructFrom(persons);
        if (jsonPayload is json) {
            checkpanic caller->respond(<@untainted json> jsonPayload);
        } else {
            checkpanic caller->respond(<@untainted string> jsonPayload.detail().message);
        }
    }
}
