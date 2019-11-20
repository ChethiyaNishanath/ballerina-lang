function foo(string b, int... c) returns int[] {
    return c;
}

public function testFunctionPointerRest() returns int[]{
   var bar = foo;
   return bar("hello", 1, 2, 3);
}

public function testFunctionPointerRestTyped() returns int[]{
   function (string b, int... c) returns int[] bar = foo;
   return bar("hello", 4, 5, 6);
}

function funcWithRestParams (int a, int b, int... c) returns [int, int, int[]] {
    return [a, b, c];
}

function testFunctionPointerAssignmentWithRestParams() returns [int, int, int[]] {
    function (int, int, int...) returns [int, int, int[]] func = funcWithRestParams;
    return func(1, 2, 3, 4);
}

function testFunctionPointerRestParamExpand() returns [int,int,int[]] {
    var bar = funcWithRestParams;
    int[] nums = [8, 9, 4];
    return bar(6, 7, ...nums);
}
