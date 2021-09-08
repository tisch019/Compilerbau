import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    Filter filter;
    List<InterpreterError> errors;


    public Parser(Filter filter, List<InterpreterError> errors) {
        this.filter = filter;
        this.errors = errors;
    }

    // compilationUnit = { decl | stmnt } EOF
    // sync part of First(decl) + First(stmnt) + EOF
    CUNode compilationUnit() throws IOException {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.EOF);
        sync.add(Token.Type.KEYDOUBLE);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);

        CUNode result = new CUNode();
        while (filter.getToken().kind != Token.Type.EOF) {
            try {
                if (filter.getToken().kind == Token.Type.KEYDOUBLE
                        || filter.getToken().kind == Token.Type.KEYINT)
                    result.add(decl(sync));
                else result.add(stmnt(sync));
            } catch (ParserError error) {
                // nothing to be done -- just proceed with decl, stmnt or EOF
            }
        }
        return result;
    }

    // decl = type IDENTIFIER SEM
    // sync: SEM
    DeclNode decl(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        Token typ = type();
        Token name = null;
        Token end = null;
        try {
            name = filter.getToken();
            filter.matchToken(Token.Type.IDENTIFIER, sync);
            end = filter.getToken();
            filter.matchToken(Token.Type.SEM, sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) {
                filter.matchToken();
                return new DeclNode(typ,name,end);
            }
            else throw error;
        }
        return new DeclNode(typ,name,end);
    }
    // type = "double" | "int" -- we already know it is int or double !
    Token type() throws IOException, ParserError {
        Token res = filter.getToken();
        filter.matchToken();
        return res;
    }

    // stmnt = ifStmnt | whileStmnt | exprStmnt | emptyStmnt | block
    //         | printStmnt
    // no syncs
    StmntNode stmnt(Set<Token.Type> synco) throws IOException, ParserError {
       Token.Type kind = filter.getToken().kind;

       StmntNode res = null;

       if (kind == Token.Type.IF) res=ifStmnt(synco);
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
    // ifStmnt = "if" "(" expr ")" stmnt [ "else" stmnt ]
    // sync:                    ^ else, First(stmnt)
    IfNode ifStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.ELSE);
        sync.add(Token.Type.IF);
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
                filter.matchToken(Token.Type.BR, sync);
                expr = expr(sync);
                filter.matchToken(Token.Type.BRC, sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.ELSE) break toElse;
                else if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
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
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.BLOCKSTART);
        sync.add(Token.Type.SEM);

        ExprNode expr = null;
        StmntNode stmnt = null;
        Token start = filter.getToken();
        filter.matchToken(); // must be "while"
        try {
            filter.matchToken(Token.Type.BR,sync);
            expr = expr(sync);
            filter.matchToken(Token.Type.BRC,sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
                    || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                    || filter.getToken().kind == Token.Type.BLOCKSTART) ;
            else throw error;
        }
        stmnt=stmnt(synco);
        return new WhileNode(start,expr,stmnt);
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

    // emptyStmnt = ";"  -- see above stmnt

    // block = "{" { decl | stmnt } "}"
    // sync: First(decl) + first(stmnt) + "}"
    BlockNode block(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.BLOCKEND);
        sync.add(Token.Type.KEYDOUBLE);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IF);
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
                if (filter.getToken().kind == Token.Type.KEYDOUBLE
                        || filter.getToken().kind == Token.Type.KEYINT)
                    res.add(decl(sync));
                else res.add(stmnt(sync));
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.BLOCKEND) break toBlockend;
                else if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
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

    // atom = DOUBLE | INT | IDENTIFIER | ("+"|"-") atom | "(" expr ")"
    // no syncs
    ExprNode atom(Set<Token.Type> synco) throws IOException, ParserError {
        ExprNode res = null;
        if (filter.getToken().kind == Token.Type.DOUBLE) {
            res = new NumberDNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.INT) {
            res = new NumberINode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.IDENTIFIER) {
            res = new IdentifierNode(filter.getToken());
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.POP) {
            Token op = filter.getToken();
            filter.matchToken();
            res = new UnOpNode(op, atom(synco));
        }
        else if (filter.getToken().kind == Token.Type.BR) {
            filter.matchToken();
            res = expr(synco);
            filter.matchToken(Token.Type.BRC, synco);
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
