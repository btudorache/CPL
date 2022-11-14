class List inherits Stringable {
    head: Element;
    tail: Element;
    size: Int <- 0;

    isEmpty(): Bool { 
        if size = 0 then true else false fi   
    };

    head(): Element { 
        if size = 0 then {
            abort();
            head;
        } else 
            head
        fi
    };

    tail(): Element { 
        if size = 0 then {
            abort();
            tail;
        } else 
            tail 
        fi
    };

    setSize(newSize: Int): Object {
        size <- newSize
    };

    size(): Int {
        size
    };

    addElement(newElement: Element): Object {
        if size = 0 then {
            head <- newElement;
            tail <- newElement;
            size <- 1;
        } else {
            tail.setNext(newElement);
            tail <- newElement;
            size <- size + 1;
        } fi
    };

    filter(filter: Filter): List { 
        let newList: List <- new List,
            headElem: Element <- head in {
            while not isvoid headElem loop {
                if filter.filter(headElem.value()) then newList.addElement(headElem) else 0 fi;
                headElem <- headElem.next();
            } pool;
            newList;
        }
    };

    -- selection sort
    sort(comparator: Comparator): Object {{
        if isvoid head then {
            self;
        } else {
            let outerRunner: Element <- head,
                currElement: Element,
                temp: Object in {
                while not isvoid outerRunner loop {
                    currElement <- outerRunner;
                    let innerRunner: Element <- outerRunner.next() in {
                        while not isvoid innerRunner loop {
                            if comparator.compareTo(currElement.value(), innerRunner.value()) then {
                                currElement <- innerRunner;
                            } else 0 fi;
                            innerRunner <- innerRunner.next();
                        } pool;
                        temp <- outerRunner.value();
                        outerRunner.setValue(currElement.value());
                        currElement.setValue(temp);
                    };
                    outerRunner <- outerRunner.next();
                } pool;
            };
        } fi;
    }};

    getAt(index: Int): Element {
        let runner: Element <- head,
            count: Int <- 0 in {
                while not count + 1 = index loop {
                    runner <- runner.next();
                    count <- count + 1;
                } pool;
                runner;
            }
    };

    toString(): String {
        let count: Int <- 0,
            io: IO <- new IO,
            listString: String,
            headElem: Element <- head in {
                if isvoid headElem then {
                    "[  ]";
                } else {
                    listString <- "[ ";
                    while count < size - 1 loop {
                        listString <- listString.concat(headElem.toString()).concat(", ");
                        headElem <- headElem.next();
                        count <- count + 1;
                    } pool;
                    listString <- listString.concat(headElem.toString()).concat(" ]");
                    listString;
                } fi;
            }
    };
};

class Element inherits Stringable {
    value: Object;
    next: Element;

    init(newValue: Object): Element {
        {
            value <- newValue;
            self;
        }
    };

    value(): Object { 
        value 
    };

    next(): Element {
        next
    };

    setNext(newNext: Element): Object {
        next <- newNext
    };

    setValue(newValue: Object): Object {
        value <- newValue
    };

    toString(): String {{
        case value of 
            dummy: String => "String(".concat(dummy).concat(")");
            dummy: Stringable => dummy.toString();
        esac;
    }};
};
