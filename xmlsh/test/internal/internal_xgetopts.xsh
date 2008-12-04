# test of xgetopts
xgetopts a:,bc:,bool,multi:+,missing: -a "Option A" -bc "Option BC" -bool -multi "Multi 1" -multi <[ <multi>Multi 2 as XML</multi> ]> Remaining <[ <args>Args</args> ]>
