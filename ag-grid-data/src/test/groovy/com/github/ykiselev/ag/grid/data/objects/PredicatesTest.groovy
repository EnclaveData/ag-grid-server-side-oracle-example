package com.github.ykiselev.ag.grid.data.objects

import com.github.ykiselev.ag.grid.api.filter.GroupKey
import com.github.ykiselev.ag.grid.api.filter.NumberColumnFilter
import com.github.ykiselev.ag.grid.api.filter.SetColumnFilter
import com.github.ykiselev.ag.grid.api.filter.TextColumnFilter
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
        cft                  | cff | exp
        'equals'             | 5   | [5]
        'notEqual'           | 5   | [1, 7]
        'lessThan'           | 7   | [1, 5]
        'lessThanOrEqual'    | 5   | [1, 5]
        'greaterThan'        | 5   | [7]
        'greaterThanOrEqual' | 5   | [5, 7]
    }

    def "should filter by number in range"() {
        given:
        def attr = Mock(Attribute)
        attr.getIntGetter() >> ({ v -> v } as ToIntFunction)
        def p = Predicates.predicate(attr, new NumberColumnFilter('inRange', 2, 6))

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
        cft           | cff   | exp
        'equals'      | 'abc' | ['abc']
        'notEqual'    | 'abc' | ['bcd', 'cde', null]
        'contains'    | 'a'   | ['abc']
        'notContains' | 'a'   | ['bcd', 'cde', null]
        'startsWith'  | 'bc'  | ['bcd']
        'endsWith'    | 'bc'  | ['abc']
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
