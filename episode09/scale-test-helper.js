module.exports = {
  notFinished: notFinished
}

function notFinished(context, next) {
  const continueLooping = context.vars.status !== 'FINISHED';
  // While `continueLooping` is true, the `next` function will
  // continue the loop in the test scenario.
  return next(continueLooping);
}
