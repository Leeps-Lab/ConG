importClass(java.lang.System)

function getMin() {
    return 0;
}

function getMax() {
    return 10;
}

function getNumStrategies() {
    return 2;
}

function getSubperiodBonus(subperiod, config) {
    return 0;
}

function getPayoff(id, percent, popStrategies, matchPopStrategies, config) {
    var sorted = []
    var yourStrategy
    for (key in popStrategies.keySet().toArray()) {
        var s = popStrategies.get(popStrategies.keySet().toArray()[key])[0]
        if (popStrategies.keySet().toArray()[key] == id) {
            yourStrategy = s
        }
        sorted.push(s)
    }
    sorted.sort(function(a, b) {return a - b})

    var i
    for (j in sorted) {
        if (sorted[j] == yourStrategy) {
            i = j
        }
    }
    i = parseInt(i)
    
  var left, right
  if (i == 0) {
    left = 0
    right = sorted[i] + 0.5 * (sorted[i + 1] - sorted[i])
  } else if (i == sorted.length - 1) {
    left = sorted[i] - 0.5 * (sorted[i] - sorted[i - 1])
    right = 1
  } else {
    left = sorted[i] - 0.5 * (sorted[i] - sorted[i - 1])
    right = sorted[i] + 0.5 * (sorted[i + 1] - sorted[i])
  }
  return 100 * (sorted[i] - left) + (right - sorted[i])
}

function getPopStrategySummary(id, percent, popStrategies, matchPopStrategies) {
    return null;
}

function getMatchStrategySummary(id, percent, popStrategies, matchPopStrategies) {
    return null;
}

function configure() {
}