# test of xgetopts
xgetopts "a=longa:,bc:,bool,multi:+,missing:" -longa "Option A" -bc "Option BC" -bool -multi "Multi 1" -multi <[ <multi>Multi 2 as XML</multi> ]> Remaining <[ <args>Args</args> ]>
