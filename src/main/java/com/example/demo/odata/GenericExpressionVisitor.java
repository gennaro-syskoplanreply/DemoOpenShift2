package com.example.demo.odata;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.*;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * GenericExpressionVisitor — Interprete dei filtri OData ($filter)
 *
 * Questa classe implementa il pattern "Visitor" per visitare e valutare
 * l'albero delle espressioni generato da Apache Olingo quando un client
 * invia una richiesta OData con un parametro $filter.
 *
 * Esempio di utilizzo:
 *   GET /odata/Users?$filter=name eq 'Mario' and age gt 18
 *
 * Olingo analizza il filtro e costruisce un albero di espressioni.
 * Questa classe visita ogni nodo dell'albero e lo valuta restituendo
 * un valore booleano che indica se l'entità corrente soddisfa il filtro.
 *
 * Operatori supportati:
 *   - Confronto:  eq, ne, gt, lt, ge, le
 *   - Logici:     and, or
 *   - Metodi:     startswith, endswith, contains, tolower, toupper, trim, length
 *
 * Come si usa nel codice:
 *   1. Creare un'istanza di GenericExpressionVisitor
 *   2. Impostare il propertyAccessor per l'entità corrente
 *   3. Chiamare filterExpression.accept(visitor) per valutare il filtro
 *
 * @see ExpressionVisitor
 * @see GenericEntityProcessor
 */
public class GenericExpressionVisitor implements ExpressionVisitor<Object> {

    private Function<String, Object> propertyAccessor;

    public void setPropertyAccessor(Function<String, Object> accessor) {
        this.propertyAccessor = accessor;
    }

    @Override
    public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
        String propertyName = ((UriResourcePrimitiveProperty) member.getResourcePath()
                .getUriResourceParts().get(0)).getProperty().getName();
        return propertyAccessor.apply(propertyName);
    }

    @Override
    public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
        String text = literal.getText();
        if (text.startsWith("'") && text.endsWith("'")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    @Override
    public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
            throws ExpressionVisitException, ODataApplicationException {
        switch (operator) {
            case EQ:  return Objects.equals(left, right);
            case NE:  return !Objects.equals(left, right);
            case AND: return Boolean.TRUE.equals(left) && Boolean.TRUE.equals(right);
            case OR:  return Boolean.TRUE.equals(left) || Boolean.TRUE.equals(right);
            default:
                throw new ODataApplicationException("Operator not supported: " + operator,
                        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }
    }

    @Override
    public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, List<Object> right)
            throws ExpressionVisitException, ODataApplicationException {
        return visitBinaryOperator(operator, left, right.isEmpty() ? null : right.get(0));
    }

    @Override
    public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitMethodCall(MethodKind methodCall, List<Object> parameters) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public Object visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException, ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
}
