(set-option :produce-proofs true)
(set-info :source "{
}")
(set-info :status unsat)
(set-info :difficulty "{ 0 }")
(set-logic AUFLIA)
(declare-fun P ( Int) Bool)
(declare-fun Q ( Int Int) Bool)
(declare-fun foo1 () Int)
(declare-fun foo2 () Int)
(declare-fun bar () Int)
(declare-fun x1 () Int)
(declare-fun x2 () Int)
(declare-fun y1 () Int)
(declare-fun y_sk () Int)
(declare-fun x3 () Int)
(declare-fun y3 () Int)
(assert (! (and (=> (P x1) (Q x1 y1)) (=> (Q x1 y_sk) (P x1)) (=> (P x2) (Q x2 y1)) (=> (Q x2 y_sk) (P x2)) (= y3 y_sk)) :named IP_0))
(assert (! (Q x3 y3) :named IP_1))
(assert (! (and (= x1 foo1) (= x2 foo2) (= x3 foo2) (= y1 bar) (or (not (P foo2)) (and (P foo1) (not (Q foo1 bar))))) :named IP_2))
(check-sat)
(get-interpolants IP_0 IP_1 IP_2)
(exit)
