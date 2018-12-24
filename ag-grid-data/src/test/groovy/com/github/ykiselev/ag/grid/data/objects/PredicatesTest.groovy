package com.github.ykiselev.ag.grid.data.objects

import com.github.ykiselev.ag.grid.api.filter.*
import com.github.ykiselev.ag.grid.data.common.Predicates
import com.github.ykiselev.ag.grid.data.types.Attribute
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function
import java.util.function.ToIntFunction

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
class PredicatesTest extends Specification {

    @Unroll
    def "should pass if number #cft #cff"() {
        given:
        def attr = Mock(Attribute)
        attr.getIntGetter() >> ({ v -> v } as ToIntFunction)
        def p = Predicates.predicate(attr, new NumberColumnFilter(cft, cff, null))

        when:
        def res = [1, 5, 7].findAll { v -> p.test(v) }

        then:
        res == exp

        where:
        cft                                    | cff | exp
        NumberFilterType.EQUALS                | 5   | [5]
        NumberFilterType.NOT_EQUAL             | 5   | [1, 7]
        NumberFilterType.LESS_THAN             | 7   | [1, 5]
        NumberFilterType.LESS_THAN_OR_EQUAL    | 5   | [1, 5]
        NumberFilterType.GREATER_THAN          | 5   | [7]
        NumberFilterType.GREATER_THAN_OR_EQUAL | 5   | [5, 7]
    }

    def "should filter by number in range"() {
        given:
        def attr = Mock(Attribute)
        attr.getIntGetter() >> ({ v -> v } as ToIntFunction)
        def p = Predicates.predicate(attr, new NumberColumnFilter(NumberFilterType.IN_RANGE, 2, 6))

        when:
        def res = [1, 2, 5, 6, 7].findAll { v -> p.test(v) }

        then:
        res == [2, 5, 6]
    }

    @Unroll
    def "should pass if text #cft '#cff'"() {
        given:
        def attr = Mock(Attribute)
        attr.getObjectGetter() >> ({ v -> v } as Function)
        def p = Predicates.predicate(attr, new TextColumnFilter(cft, cff))

        when:
        def res = ['abc', 'bcd', 'cde', null].findAll { v -> p.test(v) }

        then:
        res == exp

        where:
        cft                         | cff   | exp
        TextFilterType.EQUALS       | 'abc' | ['abc']
        TextFilterType.NOT_EQUAL    | 'abc' | ['bcd', 'cde', null]
        TextFilterType.CONTAINS     | 'a'   | ['abc']
        TextFilterType.NOT_CONTAINS | 'a'   | ['bcd', 'cde', null]
        TextFilterType.STARTS_WITH  | 'bc'  | ['bcd']
        TextFilterType.ENDS_WITH    | 'bc'  | ['abc']
    }

    def "should filter by set"() {
        given:
        def attr = Mock(Attribute)
        attr.getObjectGetter() >> ({ v -> v } as Function)
        def p = Predicates.predicate(attr, new SetColumnFilter(['a', 'b', 'c'] as Set))

        expect:
        ['a', 'c', 'f', null].findAll { v -> p.test(v) } == ['a', 'c']
    }

    def "should filter by group key"() {
        given:
        def attr = Mock(Attribute)
        attr.getObjectGetter() >> ({ v -> v } as Function)
        def p = Predicates.predicate(attr, new GroupKey('xyz'))

        expect:
        ['abc', 'xyz', null].findAll { v -> p.test(v) } == ['xyz']
    }
}
