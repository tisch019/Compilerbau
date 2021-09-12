import Bibliothek.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.util.ElementScanner14;

public class Parser {
    Filter filter;
    List<InterpreterError> errors;


    public Parser(Filter filter, List<InterpreterError> errors) {
        this.filter = filter;
        this.errors = errors;
    }


    // compilationUnit = { decl | stmnt } EOF
    // sync part of First(decl) + First(stmnt) + EOF
    CUNode compilationUnit() throws IOException{
        CUNode result = new CUNode();
        Set<Token.Type> sync = new HashSet<>();
        sync.add(Token.Type.EOF);
        sync.add(Token.Type.KEYBOOL);
        sync.add(Token.Type.KEYFA);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.KEYMAP);
        sync.add(Token.Type.KEYRA);
        sync.add(Token.Type.KEYRANGE);
        sync.add(Token.Type.KEYSET);
        sync.add(Token.Type.KEYSTATE);
        sync.add(Token.Type.KEYTRANSITION);
        sync.add(Token.Type.IFSTART);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);

        while(filter.getToken().kind != Token.Type.EOF)
        {
            try{
                // Declaration
                if(filter.getToken().kind == Token.Type.KEYBOOL
                    || filter.getToken().kind == Token.Type.KEYINT
                    || filter.getToken().kind == Token.Type.KEYFA
                    || filter.getToken().kind == Token.Type.KEYRA
                    || filter.getToken().kind == Token.Type.KEYMAP
                    || filter.getToken().kind == Token.Type.KEYRANGE
                    || filter.getToken().kind == Token.Type.KEYSET
                    || filter.getToken().kind == Token.Type.KEYSTATE
                    || filter.getToken().kind == Token.Type.KEYTRANSITION)
                    result.add(decl(sync));
                else //Statement 
                    result.add(stmnt(sync));
            }catch(ParserError error){}
        }
        return result;
    }

    // decl = type IDENTIFIER SEM
    //      TODO | type IDENTIFIER SETTO expr 
    // sync: SEM
    DeclNode decl(Set<Token.Type> synco) throws IOException, ParserError{
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        Token typ = null;
        Token name = null;
        List<Token> genericTypes = new ArrayList<Token>();
        Token sem = null;
        try{
            typ = filter.getToken();
            filter.matchToken();

            while(filter.getToken().kind != Token.Type.IDENTIFIER){
                genericTypes.add(filter.getToken());
                filter.matchToken(Token.Type.TYPE, sync);
            }

            name = filter.getToken();
            filter.matchToken(Token.Type.IDENTIFIER, sync);

            filter.matchToken(Token.Type.SEM, sync);
            
            /*
            TODO | type IDENTIFIER SETTO expr
            BinOpNode regelt SETTO
            Left = IdentifierNode, right ExprNode, op SETTO

            typ = filter.getToken();
            filter.matchToken();

            if(filter.getToken(1).kind == Token.Type.SETTO)
            {
                filter.matchToken();
                value = filter.getToken();
                filter.matchToken(Token.Type., sync);
            }*/

            
        }catch(ParserError error){
            if(filter.getToken().kind == Token.Type.SEM){
                filter.matchToken();
                return new DeclNode(typ,genericTypes,name,sem);
            }
            else throw error;
        }
        return new DeclNode(typ,genericTypes,name,sem);
    }

    // stmnt = ifStmnt | whileStmnt | exprStmnt | emptyStmnt | block
    //         | printStmnt
    // no syncs
    StmntNode stmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Token.Type kind = filter.getToken().kind;
 
        StmntNode res = null;
 
        if (kind == Token.Type.IFSTART) res=ifStmnt(synco);
        else if (kind == Token.Type.WHILE) res=whileStmnt(synco);
        else if (kind == Token.Type.BLOCKSTART) res=block(synco);
        else if (kind == Token.Type.PRINT) res=printStmnt(synco);
        else if (kind == Token.Type.SEM) {
            res = new EmptyStmntNode(filter.getToken());
            filter.matchToken();
        }
        else res=exprStmnt(synco);
        return res;
     }

    // ifStmnt = "if" "(" expr ")" stmnt [ "else" stmnt ]
    // sync:                    ^ else, First(stmnt)
    IfNode ifStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.ELSE);
        sync.add(Token.Type.IFSTART);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.BLOCKSTART);
        sync.add(Token.Type.SEM);

        ExprNode expr = null;
        StmntNode stmnt = null;
        StmntNode elseStmnt = null;
        Token start = filter.getToken();
        filter.matchToken(); // must be "if"
        toElse: {
            try {
                filter.matchToken(Token.Type.BRACKETSTART, sync);
                expr = expr(sync);
                filter.matchToken(Token.Type.BRACKETEND, sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.ELSE) break toElse;
                else if (filter.getToken().kind == Token.Type.IFSTART || filter.getToken().kind == Token.Type.WHILE
                        || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                        || filter.getToken().kind == Token.Type.BLOCKSTART) ;
                else throw error;
            }
            sync = new HashSet<>(synco);
            sync.add(Token.Type.ELSE);
            try {
                stmnt = stmnt(sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.ELSE) ;
                else throw error;
            }
        }
        if (filter.getToken().kind == Token.Type.ELSE) {
            filter.matchToken();
            elseStmnt=stmnt(synco);
        }
        return new IfNode(start, expr, stmnt, elseStmnt);
    }

    // whileStmnt = "while" "(" expr ")" stmnt
    // sync:                          ^ else, First(stmnt)
    WhileNode whileStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.IFSTART);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.BLOCKSTART);
        sync.add(Token.Type.SEM);

        ExprNode expr = null;
        StmntNode stmnt = null;
        Token start = filter.getToken();
        filter.matchToken(); // must be "while"
        try {
            filter.matchToken(Token.Type.BRACKETSTART,sync);
            expr = expr(sync);
            filter.matchToken(Token.Type.BRACKETEND,sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.IFSTART || filter.getToken().kind == Token.Type.WHILE
                    || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                    || filter.getToken().kind == Token.Type.BLOCKSTART) ;
            else throw error;
        }
        stmnt=stmnt(synco);
        return new WhileNode(start,expr,stmnt);
    }

    // block = "{" { decl | stmnt } "}"
    // sync: First(decl) + first(stmnt) + "}"
    BlockNode block(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.BLOCKEND);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IFSTART);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);

        Token start = filter.getToken();
        BlockNode res = new BlockNode(start,null);

        filter.matchToken(); // must be "{"
        toBlockend: while (filter.getToken().kind != Token.Type.BLOCKEND
                && filter.getToken().kind != Token.Type.EOF
        ) {
            try {
                if (filter.getToken().kind == Token.Type.KEYBOOL
                    || filter.getToken().kind == Token.Type.KEYINT
                    || filter.getToken().kind == Token.Type.KEYFA
                    || filter.getToken().kind == Token.Type.KEYRA
                    || filter.getToken().kind == Token.Type.KEYMAP
                    || filter.getToken().kind == Token.Type.KEYRANGE
                    || filter.getToken().kind == Token.Type.KEYSET
                    || filter.getToken().kind == Token.Type.KEYSTATE
                    || filter.getToken().kind == Token.Type.KEYTRANSITION)
                    res.add(decl(sync));
                else res.add(stmnt(sync));
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.BLOCKEND) break toBlockend;
                else if (filter.getToken().kind == Token.Type.IFSTART || filter.getToken().kind == Token.Type.WHILE
                        || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                        || filter.getToken().kind == Token.Type.BLOCKSTART) ;
                else throw error;
            }
        }
        sync = new HashSet<>(synco);
        sync.add(Token.Type.BLOCKEND);
        try {
            res.end = filter.getToken();
            filter.matchToken(Token.Type.BLOCKEND, sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.BLOCKEND) filter.matchToken();
            else throw error;
        }
        return res;
    }

    // printStmnt = "print" expr ";"
    // sync: ";"
    PrintNode printStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        Token start = filter.getToken();
        Token end = null;
        ExprNode expr = null;
        filter.matchToken();
        try {
            expr = expr(sync);
            end = filter.getToken();
            filter.matchToken(Token.Type.SEM,sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) {
                filter.matchToken();
                return null;
            }
            else throw error;
        }
        return new PrintNode(start,end,expr);
    }

    // exprStmnt = expr ";"
    // sync: ";"
    ExprStmntNode exprStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        Token end = null;
        ExprNode expr = null;
        try {
            expr = expr(sync);
            end = filter.getToken();
            filter.matchToken(Token.Type.SEM, sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) ;
            else throw error;
        }
        return new ExprStmntNode(expr, end);
    }

    // expr = IDENTIFIER "=" expr | comp
    // sync:            ^ "="
    ExprNode expr(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.SETTO);

        ExprNode res = null;
        if (filter.getToken(1).kind == Token.Type.SETTO) {
            ExprNode left = null;
            ExprNode right = null;
            Token op = null;
            try {
                op = filter.getToken(1);
                left = new IdentifierNode(filter.getToken());
                filter.matchToken(Token.Type.IDENTIFIER, sync);
            } catch (ParserError error) {
                // ignore whatever there is on the left hand side
            }
            filter.matchToken();
            right = expr(synco);
            res = new BinOpNode(op,left,right);
        }
        else res = comp(synco);
        return res;
    }

    // comp = sum "<" sum | sum
    // sync:      ^ "<"
    ExprNode comp(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.COMP);

        ExprNode res = null;
        try {
            res = sum(sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.COMP) ;
            else throw error;
        }
        if (filter.getToken().kind == Token.Type.COMP) {
            ExprNode left = res;
            Token op = filter.getToken();
            filter.matchToken();
            ExprNode right = sum(synco);
            res = new BinOpNode(op,left,right);
        }
        return res;
    }

    // sum = { prod { ("+"|"-") prod } }
    // no syncs ???
    ExprNode sum(Set<Token.Type> synco) throws IOException, ParserError {
        ExprNode res =  prod(synco);
        while (filter.getToken().kind == Token.Type.POP) {
            Token op = filter.getToken();
            filter.matchToken();
            ExprNode left = res;
            ExprNode right = prod(synco);
            res = new BinOpNode(op,left,right);
        }
        return res;
    }

    // prod = { atom { ("*"|"/"|"%") atom } }
    // sync:         ^ "*" ...
    ExprNode prod(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.LOP);
        ExprNode res = null;

        try {
            res = atom(sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.LOP) ;
            else throw error;
        }
        while (filter.getToken().kind == Token.Type.LOP) {
            Token op = filter.getToken();
            filter.matchToken();
            ExprNode left = res;
            ExprNode right = atom(synco);
            res = new BinOpNode(op,left,right);
        }
        return res;
    }

    // atom = INT | BOOL | STRING | CHAR | RANGE | STATE | TRANSITION | FA | RA 
    //       | SET | MAP | ARRAY 
    //       | IDENTIFIER | ("+"|"-") atom | "(" expr ")"
    // no syncs
    ExprNode atom(Set<Token.Type> synco) throws IOException, ParserError {
        ExprNode res = null;

        if (filter.getToken().kind == Token.Type.INT) {
            res = new NumberINode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.BOOL){
            res = new BoolNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.STRING){
            res = new StringNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.CHAR){
            res = new CharNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.SQUAREBRACKETOPEN){
            //Range
            filter.matchToken();
            Pair<ExprNode,ExprNode> range;
            List<Pair<ExprNode,ExprNode>> entries = new ArrayList<Pair<ExprNode,ExprNode>>();
            while(filter.getToken().kind != Token.Type.SQUAREBRACKETCLOSE){
                if(filter.getToken(1).kind == Token.Type.POP){ // -
                    ExprNode l = expr(synco);
                    filter.matchToken(); // -
                    ExprNode r = expr(synco);
                    range = new Pair<ExprNode,ExprNode>(l,r);
                }else{
                    range = new Pair<ExprNode,ExprNode>(expr(synco),null);
                }
                entries.add(range);
                if(filter.getToken().kind == Token.Type.COMMA)
                    filter.matchToken();
            }
            filter.matchToken(); // ]
            res = new RangeNode(entries);
        }
        else if(filter.getToken().kind == Token.Type.MAPARRAYSTART){
            if(filter.getToken(1).kind == Token.Type.MAPDELI){
                //Map Element:Value
                List<Pair<ExprNode,ExprNode>> elements = new ArrayList<Pair<ExprNode,ExprNode>>();
                Pair<ExprNode,ExprNode> pair;
                while(filter.getToken().kind != Token.Type.MAPARRAYEND){
                    ExprNode l = expr(synco);
                    filter.matchToken(Token.Type.MAPDELI, synco);
                    ExprNode r = expr(synco);
                    pair = new Pair<ExprNode,ExprNode>(l,r);
                    elements.add(pair);
                }
                filter.matchToken(); // ]]
                //TODO
                //res = new MapNode(elements);
            }else{
                //Array
                List<ExprNode> elements = new ArrayList<ExprNode>();
                while(filter.getToken().kind != Token.Type.MAPARRAYEND){
                    elements.add(expr(synco));
                    if(filter.getToken().kind != Token.Type.MAPARRAYEND)
                        filter.matchToken(Token.Type.COMMA, synco);
                }
                
                filter.matchToken(); // ]]
                //TODO
                //res = new ArrayNode(elements);
            }
        }
        else if (filter.getToken().kind == Token.Type.STATE){
            StateNode stateA;
            Token content = filter.getToken();
            filter.matchToken();
            if(filter.getToken().kind == Token.Type.STATEACC){
                stateA = new StateNode(content, true);
                filter.matchToken();
            }
            else
                stateA = new StateNode(content,false);

            if(filter.getToken().kind == Token.Type.TRANSSTART
                || filter.getToken().kind == Token.Type.TRANSEPSI){
                //Transition
                if(filter.getToken().kind == Token.Type.TRANSSTART){
                    //Trans = STATE TRANSSTART RANGE TRANSEND STATE
                    filter.matchToken(); // --
                    ExprNode r = expr(synco);
                    filter.matchToken(Token.Type.TRANSEND,synco); // -->
                    ExprNode stateB = expr(synco);
                    res = new TransitionNode(stateA,r,stateB);
                }else if(filter.getToken().kind == Token.Type.TRANSEPSI){
                    //TransEpsi = STATE TRANSEPSI STATE
                    filter.matchToken(); // --->
                    ExprNode stateB = expr(synco);
                    res = new TransitionNode(stateA,stateB);
                }
            }else{ //No Transition but State
                res = stateA;
            }
        }
        //TODO RA
        //TODO FA
        //TODO SET
        else if (filter.getToken().kind == Token.Type.IDENTIFIER) {
            res = new IdentifierNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.POP) {
            Token op = filter.getToken();
            filter.matchToken();
            res = new UnOpNode(op, atom(synco));
        }
        else if (filter.getToken().kind == Token.Type.BRACKETSTART) {
            filter.matchToken();
            res = expr(synco);
            filter.matchToken(Token.Type.BRACKETEND, synco);
        }
        else {
            Token currentToken = filter.getToken();
            ParserError error = new ParserError(currentToken, "Expression expected");
            errors.add(error);
            // panic !!!
            while (!synco.contains(filter.getToken().kind)) filter.matchToken();
            throw error;
        }
        return res;
    }
}
