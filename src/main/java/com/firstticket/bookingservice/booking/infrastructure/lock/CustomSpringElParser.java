package com.firstticket.bookingservice.booking.infrastructure.lock;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CustomSpringElParser {
    public static String getDynamicValue(String[] parameterNames, Object[] args, String key) {

        if (parameterNames == null || args == null || parameterNames.length != args.length) {
            throw new IllegalArgumentException("SpEL 파라미터 이름/값 매핑이 유효하지 않습니다.");
        }

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, String.class);
    }
}
