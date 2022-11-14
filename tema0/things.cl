(*******************************
 *** Classes Product-related ***
 *******************************)
class Stringable {
    toString(): String { "not implemented" };
};

class Product inherits Stringable {
    name : String;
    model : String;
    price : Int;

    init(n : String, m: String, p : Int):SELF_TYPE {{
        name <- n;
        model <- m;
        price <- p;
        self;
    }};

    getprice():Int{ price * 119 / 100 };

    toString(): String { 
        type_name().concat("(").concat(name).concat(";").concat(model).concat(")")
    };
};

class Edible inherits Product {
    -- VAT tax is lower for foods
    getprice():Int { price * 109 / 100 };
};

class Soda inherits Edible {
    -- sugar tax is 20 bani
    getprice():Int {price * 109 / 100 + 20};
};

class Coffee inherits Edible {
    -- this is technically poison for ants
    getprice():Int {price * 119 / 100};
};

class Laptop inherits Product {
    -- operating system cost included
    getprice():Int {price * 119 / 100 + 499};
};

class Router inherits Product {};

(****************************
 *** Classes Rank-related ***
 ****************************)
class Rank inherits Stringable {
    name : String;
    rankValue: Int;

    init(n : String): SELF_TYPE {
        {
            name <- n;
            rankValue <- 0;
            self;
        }
    };

    getRankValue(): Int {
        rankValue
    };

    toString(): String {
        type_name().concat("(").concat(name).concat(")")
    };
};

class Private inherits Rank {
    init(n : String): SELF_TYPE {
        {
            name <- n;
            rankValue <- 1;
            self;
        }
    };
};

class Corporal inherits Private {
    init(n : String): SELF_TYPE {
        {
            name <- n;
            rankValue <- 2;
            self;
        }
    };
};

class Sergent inherits Corporal {
    init(n : String): SELF_TYPE {
        {
            name <- n;
            rankValue <- 3;
            self;
        }
    };
};

class Officer inherits Sergent {
    init(n : String): SELF_TYPE {
        {
            name <- n;
            rankValue <- 4;
            self;
        }
    };
};

class CustomInt inherits Stringable {
    value: String;

    init(newValue: String): SELF_TYPE {{
        value <- newValue;
        self;
    }};

    toString(): String {
        "Int(".concat(value).concat(")")
    };
};

class CustomBool inherits Stringable {
    value: String;

    init(newValue: String): SELF_TYPE {{
        value <- newValue;
        self;
    }};

    toString(): String {
        "Bool(".concat(value).concat(")")
    };
};

class CustomIO inherits Stringable {
    io: IO;

    init(): SELF_TYPE {{
        io <- new IO;
        self;
    }};

    toString(): String {
        "IO()"
    };
};
