(* Think of these as abstract classes *)
class Comparator {
    order: String;

    init(newOrder: String): SELF_TYPE {{
        if newOrder = "ascendent" then order <- newOrder else
        if newOrder = "descendent" then order <- newOrder else abort() fi fi;
        self;
    }};

    compareTo(o1 : Object, o2 : Object): Bool {true};
};

class PriceComparator inherits Comparator {
    compareTo(o1 : Object, o2 : Object): Bool {
        case o1 of 
            dummy1: Product => 
                case o2 of 
                    dummy2: Product => if order = "ascendent" then {
                        dummy2.getprice() < dummy1.getprice();
                    } else {
                        dummy1.getprice() < dummy2.getprice();
                    } fi;
                    dummy2: Object => {abort(); true;};
                esac;
            dummy1: Object => {abort(); true;};
        esac
    };
};

class RankComparator inherits Comparator {
    compareTo(o1 : Object, o2 : Object): Bool {
        case o1 of 
            dummy1: Rank => 
                case o2 of 
                    dummy2: Rank => if order = "ascendent" then {
                        dummy2.getRankValue() < dummy1.getRankValue();
                    } else {
                        dummy1.getRankValue() < dummy2.getRankValue();
                    } fi;
                    dummy2: Object => {abort(); true;};
                esac;
            dummy1: Object => {abort(); true;};
        esac
    };
};

class AlphabeticComparator inherits Comparator {
    compareTo(o1 : Object, o2 : Object): Bool {
        case o1 of 
            dummy1: String => 
                case o2 of 
                    dummy2: String => if order = "ascendent" then {
                        dummy2 < dummy1;
                    } else {
                        dummy1 < dummy2;
                    } fi;
                    dummy2: Object => {abort(); true;};
                esac;
            dummy1: Object => {abort(); true;};
        esac
    };
};

class Filter {
    filter(o : Object):Bool {true};
};

class ProductFilter inherits Filter {
    filter(o : Object): Bool {{
        let value: Bool in {
            case o of
                dummy: Product => value <- true;
                dummy: Object => value <- false;
            esac;
            value;
        };
    }};
};

class RankFilter inherits Filter {
    filter(o : Object): Bool {{
        let value: Bool in {
            case o of
                dummy: Rank => value <- true;
                dummy: Object => value <- false;
            esac;
            value;
        };
    }};
};

class SamePriceFilter inherits Filter {
    filter(o : Object): Bool {{
        let value: Bool in {
            case o of
                dummy: Product => {
                    if dummy.getprice() = dummy@Product.getprice() then value <- true else value <- false fi;
                };
                dummy: Object => value <- false;
            esac;
            value;
        };
    }};
};

(* TODO: implement specified comparators and filters*)

class StrTokenizer inherits IO {
    tokens: List;
    tokensCount: Int;
    runner: Element;

    init(line: String): SELF_TYPE { 
        let stringLength: Int <- line.length(),
            index: Int <- 0,
            wordStart: Int <- 0 in 
            {   
                tokens <- new List;
                while index < stringLength loop {
                    if line.substr(index, 1) = " " then {
                        tokensCount <- tokensCount + 1;
                        tokens.addElement((new Element).init(line.substr(wordStart, index - wordStart)));
                        wordStart <- index + 1;
                    } else {
                        self;
                    } fi;
                    index <- index + 1;
                } pool;

                tokens.addElement((new Element).init(line.substr(wordStart, index - wordStart)));
                tokensCount <- tokensCount + 1;
    
                runner <- tokens.head();
                self;
            }
    };

    tokensCount(): Int {
        tokensCount
    };

    hasMoreTokens(): Bool {
        isvoid runner
    };

    nextToken(): String {
        let nextToken: String in {
            case runner.value() of token: String => nextToken <- token; esac;
            runner <- runner.next();
            nextToken;
        }
    };
};

(* a2i taken from the example programs *)
class Utils {
    parseFilter(filterString: String): Filter {
        let filter: Filter in {
            if filterString = "ProductFilter" then filter <- new ProductFilter else
            if filterString = "RankFilter" then filter <- new RankFilter else
            if filterString = "SamePriceFilter" then filter <- new SamePriceFilter else abort()
            fi fi fi;
            filter;
        }
    };

    parseComparator(sortString: String, order: String): Comparator {
        let comparator: Comparator in {
            if sortString = "PriceComparator" then comparator <- new PriceComparator.init(order) else
            if sortString = "RankComparator" then comparator <- new RankComparator.init(order) else
            if sortString = "AlphabeticComparator" then comparator <- new AlphabeticComparator.init(order) else abort()
            fi fi fi;
            comparator;
        }
    };

    a2i(s : String) : Int {
        if s.length() = 0 then 0 else
	    if s.substr(0,1) = "-" then ~a2i_aux(s.substr(1,s.length()-1)) else
        if s.substr(0,1) = "+" then a2i_aux(s.substr(1,s.length()-1)) else
           a2i_aux(s)
        fi fi fi
    };

    a2i_aux(s : String) : Int {
	(let int : Int <- 0 in	
           {	
               (let j : Int <- s.length() in
	          (let i : Int <- 0 in
		    while i < j loop
			{
			    int <- int * 10 + c2i(s.substr(i,1));
			    i <- i + 1;
			}
		    pool
		  )
	       );
              int;
	    }
        )
     };

    c2i(char : String) : Int {
        if char = "0" then 0 else
        if char = "1" then 1 else
        if char = "2" then 2 else
        if char = "3" then 3 else
        if char = "4" then 4 else
        if char = "5" then 5 else
        if char = "6" then 6 else
        if char = "7" then 7 else
        if char = "8" then 8 else
        if char = "9" then 9 else
        { abort(); 0; }
        fi fi fi fi fi fi fi fi fi fi
    };
};
