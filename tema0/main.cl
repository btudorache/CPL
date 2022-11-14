class Main inherits IO {
    lists : List <- new List;
    looping : Bool <- true;
    utils: Utils <- new Utils;

    main(): Object {
        let lineTokens: StrTokenizer,
            looping : Bool <- true,
            command: String,
            printIndex: Int in {
                load_input();
                while looping loop {
                    lineTokens <- (new StrTokenizer).init(in_string());
                    command <- lineTokens.nextToken();
                    
                    if command = "help" then show_commands() else 
                    if command = "load" then load_input() else
                    if command = "print" then {
                        if lineTokens.tokensCount() = 2 then printIndex <- utils.a2i(lineTokens.nextToken()) else printIndex <- 0 fi;
                        print_command(printIndex);
                    } else
                    if command = "merge" then merge_command(utils.a2i(lineTokens.nextToken()), utils.a2i(lineTokens.nextToken())) else 
                    if command = "filterBy" then filter_command(utils.a2i(lineTokens.nextToken()), utils.parseFilter(lineTokens.nextToken())) else 
                    if command = "sortBy" then sort_command(utils.a2i(lineTokens.nextToken()), utils.parseComparator(lineTokens.nextToken(), lineTokens.nextToken())) else abort()
                    fi fi fi fi fi fi;
                } pool;
        }
    };

    show_commands(): IO {
        out_string("Available commands:\n")
        .out_string("help\n")
        .out_string("load\n")
        .out_string("print [index]\n")
        .out_string("merge index1 index2\n")
        .out_string("filterBy index {ProductFilter,RankFilter,SamePriceFilter}\n")
        .out_string("sortBy index {PriceComparator,RankComparator,AlphabeticComparator} {ascendent,descendent}\n")
    };

    load_input(): Object {
        let newList: List <- new List in {
            let lineTokens: StrTokenizer,
                looping: Bool <- true,
                item: String,
                name: String,
                model: String,
                price: Int in
                while looping loop {
                    lineTokens <- (new StrTokenizer).init(in_string());
                    item <- lineTokens.nextToken();
                    
                    if lineTokens.tokensCount() = 1 then 0 else
                    if lineTokens.tokensCount() = 2 then {
                        name <- lineTokens.nextToken();
                    } else if lineTokens.tokensCount() = 4 then {
                        name <- lineTokens.nextToken();
                        model <- lineTokens.nextToken();
                        price <- utils.a2i(lineTokens.nextToken());
                    } else 0
                    fi fi fi;

                    if item = "Soda" then load_product(newList, item, name, model, price) else 
                    if item = "Coffee" then load_product(newList, item, name, model, price) else 
                    if item = "Laptop" then load_product(newList, item, name, model, price) else
                    if item = "Router" then load_product(newList, item, name, model, price) else
                    if item = "Private" then load_rank(newList, item, name) else 
                    if item = "Corporal" then load_rank(newList, item, name) else 
                    if item = "Sergent" then load_rank(newList, item, name) else
                    if item = "Officer" then load_rank(newList, item, name) else
                    if item = "Int" then newList.addElement((new Element).init((new CustomInt).init(name))) else
                    if item = "String" then newList.addElement((new Element).init(name)) else
                    if item = "Bool" then newList.addElement((new Element).init((new CustomBool).init(name))) else
                    if item = "IO" then newList.addElement((new Element).init((new CustomIO).init())) else
                    if item = "END" then looping <- false else abort() 
                    fi fi fi fi fi fi fi fi fi fi fi fi fi;
                } pool;
                lists.addElement((new Element).init(newList));
            }
    };

    load_rank(list: List, rank: String, name: String): Object {
        let newRank: Rank in {
            if rank = "Private" then newRank <- (new Private).init(name) else 
            if rank = "Corporal" then newRank <- (new Corporal).init(name) else 
            if rank = "Sergent" then newRank <- (new Sergent).init(name) else 
            if rank = "Officer" then newRank <- (new Officer).init(name) else abort()
            fi fi fi fi;
            list.addElement((new Element).init(newRank));
        }
    };

    load_product(list: List, product: String, name: String, model: String, price: Int): Object {
        let newProduct: Product in {
            if product = "Soda" then newProduct <- (new Soda).init(name, model, price) else 
            if product = "Coffee" then newProduct <- (new Coffee).init(name, model, price) else 
            if product = "Laptop" then newProduct <- (new Laptop).init(name, model, price) else 
            if product = "Router" then newProduct <- (new Router).init(name, model, price) else abort()
            fi fi fi fi;
            list.addElement((new Element).init(newProduct));
        }
    };

    print_command(indexOptional: Int): Object {
        if indexOptional = 0 then {
            let runner: Element <- lists.head(),
                count: Int <- 0 in {
                while count < lists.size() loop {
                    out_int(count + 1).out_string(": ").out_string(runner.toString()).out_string("\n");
                    runner <- runner.next();
                    count <- count + 1;
                } pool;
            };
        } else {
            if indexOptional <= lists.size() then {
                let elem: Element <- lists.getAt(indexOptional) in {
                    out_string(elem.toString()).out_string("\n");
                };
            } else abort() fi;
        } fi
    };

    merge_command(index1: Int, index2: Int): Object {
        let runner: Element <- lists.head(),
            count: Int <- 0,
            newList: List <- new List,
            list1: List,
            list2: List in {
                while not count = lists.size() loop {
                    if count + 1 = index1 then {
                        case runner.value() of
                            dummy: List => list1 <- dummy;
                        esac;
                    } else if count + 1 = index2 then {
                        case runner.value() of
                            dummy: List => list2 <- dummy;
                        esac;
                    } else {
                        newList.addElement(runner);
                    } fi fi;
                    count <- count + 1;
                    runner <- runner.next();
                } pool;
                list1.tail().setNext(list2.head());
                list1.setSize(list1.size() + list2.size());
                newList.addElement((new Element).init(list1));
                lists <- newList;
            }
    };

    filter_command(index: Int, filter: Filter): Object {
        let element: Element <- lists.getAt(index) in {
            case element.value() of
                dummy: List => element.setValue(dummy.filter(filter));
            esac;
        }
    };

    sort_command(index: Int, comparator: Comparator): Object {
        let element: Element <- lists.getAt(index) in {
            case element.value() of
                dummy: List => dummy.sort(comparator);
            esac;
        }
    };

    invalid_command(): Object {{
        out_string("Invalid command. Aborting program\n");
        looping <- false;
        abort();
    }};
};
